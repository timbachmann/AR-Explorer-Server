package de.timbachmann.capvisar.database

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File

@Serializable
data class Config(val server: WebServerConfig) {

    companion object {

        private const val DEFAULT_CONFIG_FILE = "config.json"

        fun readConfig(config: String = DEFAULT_CONFIG_FILE): Config {
            val jsonString = File(config).readText()
            return Json.decodeFromString(serializer(), jsonString)
        }

    }

}
