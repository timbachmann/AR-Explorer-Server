package de.timbachmann.capvisar.database

import de.timbachmann.capvisar.model.image.Image
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.net.URI
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.concurrent.atomic.AtomicInteger

class ImageDao {

    private val IMAGES_PATH = "/Users/tim/Documents/CapVisARServer/images"

    private var lastId: AtomicInteger = AtomicInteger(File(IMAGES_PATH).walkTopDown().count()-1)

    init {
        if (Files.notExists(Paths.get(IMAGES_PATH))) {
            Files.createDirectory(Paths.get(IMAGES_PATH))
        }
    }

    fun save(name: String, data: ByteArray, lat: Double, lng: Double, date: String, source: String, bearing: Int) {
        val id = lastId.incrementAndGet()
        val image = Image(name = name, id = id, data = data, lat = lat, lng = lng, date = date, source = source, bearing = bearing)
        val string = Json.encodeToString(image)
        File(IMAGES_PATH + "/${id}.json").writeText(string)
    }

    private val json = Json { ignoreUnknownKeys = true }

    fun getList(): List<Image> {
        val imageList: MutableList<Image> = mutableListOf<Image>()
        File(IMAGES_PATH).walkTopDown().forEach {
            if (it.toString().endsWith(".json")) {
                imageList += json.decodeFromString<Image>(it.readText())
            }
        }
        return imageList
    }
}