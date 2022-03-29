package de.timbachmann.capvisar.database

import de.timbachmann.capvisar.model.api.response.ImageListResponse
import de.timbachmann.capvisar.model.image.ApiImage
import de.timbachmann.capvisar.model.image.MetaData
import de.timbachmann.capvisar.rest.logger
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.coobird.thumbnailator.Thumbnails
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class ImageDao {
    private val path = "/home/tim/CapVis/images"
    private val json = Json { ignoreUnknownKeys = true }

    /**
     * Create image directory if not already created.
     */
    init {
        if (Files.notExists(Paths.get(path))) {
            Files.createDirectory(Paths.get(path))
        }
    }

    /**
     * Saves an ApiImage to JSON file
     */
    fun save(id: String, data: ByteArray, lat: Double, lng: Double, date: String, source: String, bearing: Int) {
        logger.info {"Saving image to file..."}
        val metaData = MetaData(id = id, lat = lat, lng = lng, date = date, source = source, bearing = bearing)
        val imagePath = path + "/${id}"
        logger.info { "Meta created!" }
        if (Files.notExists(Paths.get(imagePath))) {
            Files.createDirectory(Paths.get(imagePath))
        }
        val metaString = Json.encodeToString(metaData)
        println(metaString)
        val metaFile = File(imagePath + "/${id}.json")
        metaFile.writeText(metaString)
        logger.info { "Meta data saved!" }
        val imageFile = File(imagePath + "/${id}.jpg")
        imageFile.writeBytes(data)
        logger.info { "Image Saved!" }
        resizeImage(imageFile, id)
        logger.info { "Image $id saved!" }
    }

    private fun resizeImage(originalImage: File, id: String) {
        try {
            logger.info { "Creating Thumbnail..." }
            Thumbnails.of(originalImage)
                .size(256, 256)
                .outputFormat("jpg")
                .outputQuality(0.8)
                .toFile(File(path + "/${id}/${id}-thumb.jpg"))
            logger.info { "Thumbnail created!" }
        } catch (error: IOException) {
            println(error)
        }
    }

    /**
     * Retrieves all images stored in image directory and returns them
     * @return ImageListResponse
     */
    fun getList(): ImageListResponse {
        logger.info {"Retrieving images..."}
        val apiImageLists: MutableList<ApiImage> = mutableListOf()
        File(path).walkTopDown().forEach {
            if (it.toString().endsWith("thumb.jpg")) {
                val metaData = json.decodeFromString<MetaData>(File(it.toString().replace("-thumb.jpg", ".json")).readText())
                val imageBytes = Files.readAllBytes(Paths.get(it.toURI()))
                apiImageLists += ApiImage(id = metaData.id, data = byteArrayOf(), thumbnail = imageBytes, lat = metaData.lat, lng = metaData.lng,
                    date = metaData.date, source = metaData.source, bearing = metaData.bearing)
            }
        }
        logger.info {"Sent image list response!"}
        return ImageListResponse(apiImages = apiImageLists.toTypedArray())
    }

    /**
     * Retrieves a single image by id and returns it
     * @return ApiImage
     */
    fun getApiImageById(id: String): ApiImage? {
        logger.info {"Retrieving image..."}
        if (Files.exists(Paths.get(path + "/${id}"))) {
            val fullImage = File(path + "/${id}/${id}.jpg")
            val metaData = json.decodeFromString<MetaData>(File(fullImage.toString().replace(".jpg", ".json")).readText())
            val imageBytes = Files.readAllBytes(Paths.get(fullImage.toURI()))
            logger.info {"Image response sent!"}
            return ApiImage(id = metaData.id, data = imageBytes, thumbnail = byteArrayOf(), lat = metaData.lat, lng = metaData.lng,
                date = metaData.date, source = metaData.source, bearing = metaData.bearing)
        }
        logger.info {"Image not found!"}
        return null
    }

    /**
     * Deletes a single image by id
     * @return ApiImage
     */
    fun deleteApiImageById(id: String) {
        logger.info {"Retrieving image..."}
        if (Files.exists(Paths.get(path + "/${id}"))) {
            Files.walk(Paths.get(path + "/${id}"))
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete)
        }
        logger.info {"Image does not exist!"}
    }
}