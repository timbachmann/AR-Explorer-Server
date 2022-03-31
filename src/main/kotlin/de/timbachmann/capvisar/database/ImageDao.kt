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
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

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
     * Retrieves all images matching given filter stored in image directory and returns them
     * @return ImageListResponse
     */
    fun getListWithFilter(startDate: String?, endDate: String?, lat: Double?, lng: Double?, radius: Int?): ImageListResponse {
        logger.info {"Retrieving images matching filter..."}
        val apiImageLists: MutableList<ApiImage> = mutableListOf()
        File(path).walkTopDown().forEach {
            if (it.toString().endsWith("thumb.jpg")) {
                var isMatchDate = false
                var isMatchRadius = false
                val metaData = json.decodeFromString<MetaData>(File(it.toString().replace("-thumb.jpg", ".json")).readText())
                if (startDate != null && endDate != null) {
                    val pattern: DateTimeFormatter = DateTimeFormatter.ofPattern( "yyyy-MM-dd'T'HH:mm:ssZZZZZ")
                    val start: LocalDate = LocalDate.parse(startDate, pattern)
                    val end: LocalDate = LocalDate.parse(endDate, pattern)
                    val imageDate: LocalDate = LocalDate.parse(metaData.date, pattern)
                    if (imageDate.isAfter(start) && imageDate.isBefore(end)) {
                        isMatchDate = true
                    }
                }
                if (lat != null && lng != null && radius != null) {
                    isMatchRadius = haversineDistance(lat, lng, metaData.lat, metaData.lng) < radius
                }
                if (isMatchDate && isMatchRadius) {
                    val imageBytes = Files.readAllBytes(Paths.get(it.toURI()))
                    apiImageLists += ApiImage(id = metaData.id, data = byteArrayOf(), thumbnail = imageBytes, lat = metaData.lat, lng = metaData.lng,
                        date = metaData.date, source = metaData.source, bearing = metaData.bearing)
                }
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

    /**
     * Calculates haversine distance between two lat/lng points
     * @param lat1
     * @param lon1
     * @param lat2
     * @param lon2
     * @return
     */
    private fun haversineDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371e3
        val phi1: Double = degreesToRadians(lat1)
        val phi2: Double = degreesToRadians(lat2)
        val deltaPhi: Double = degreesToRadians(lat2 - lat1)
        val deltaLambda: Double = degreesToRadians(lon2 - lon1)
        val a = sin(deltaPhi / 2.0) * sin(deltaPhi / 2.0) + cos(phi1) * cos(phi2) *
                sin(deltaLambda / 2.0) * sin(deltaLambda / 2.0)
        val c = 2.0 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }

    private fun degreesToRadians(angle: Double): Double {
        return angle * (Math.PI / 180.0)
    }
}