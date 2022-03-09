package de.timbachmann.capvisar.database

import de.timbachmann.capvisar.model.api.response.ImageListResponse
import de.timbachmann.capvisar.model.image.ApiImage
import de.timbachmann.capvisar.rest.logger
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.coobird.thumbnailator.Thumbnails
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.lang.Exception
import java.nio.file.Files
import java.nio.file.Paths
import javax.imageio.ImageIO

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
        val apiImage = ApiImage(id = id, data = data, lat = lat, lng = lng, date = date, source = source, bearing = bearing)
        val string = Json.encodeToString(apiImage)
        File(path + "/${id}.json").writeText(string)
        logger.info { "Image $id saved!" }
    }

    /**
     * Retrieves all images stored in image directory and returns them
     * @return ImageListResponse
     */
    fun getList(): ImageListResponse {
        logger.info {"Retrieving images..."}
        val apiImageLists: MutableList<ApiImage> = mutableListOf()
        File(path).walkTopDown().forEach {
            if (it.toString().endsWith(".json")) {
                val apiImage: ApiImage = json.decodeFromString<ApiImage>(it.readText())
                apiImage.data = resizeImage(ImageIO.read(ByteArrayInputStream(apiImage.data)))
                apiImageLists += apiImage
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
        File(path).walkTopDown().forEach {
            if (it.toString().contains(id)) {
                logger.info {"Sent image response!"}
                return json.decodeFromString<ApiImage>(it.readText())
            }
        }
        logger.info {"Image not found!"}
        return null
    }

    private fun resizeImage(originalImage: BufferedImage): ByteArray {
        val outputStream = ByteArrayOutputStream()
        Thumbnails.of(originalImage)
            .outputFormat("JPEG")
            .outputQuality(0.9)
            .toOutputStream(outputStream)
        return outputStream.toByteArray()
    }


}