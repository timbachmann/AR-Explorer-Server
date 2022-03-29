package de.timbachmann.capvisar.rest

import cc.vileda.openapi.dsl.info
import cc.vileda.openapi.dsl.openapiDsl
import de.timbachmann.capvisar.database.Config
import de.timbachmann.capvisar.model.api.response.ErrorResponse
import de.timbachmann.capvisar.rest.handlers.ImageHandler
import io.javalin.Javalin
import io.javalin.apibuilder.ApiBuilder.*
import io.javalin.plugin.openapi.OpenApiOptions
import io.javalin.plugin.openapi.OpenApiPlugin
import io.javalin.plugin.openapi.ui.ReDocOptions
import io.javalin.plugin.openapi.ui.SwaggerOptions
import mu.KotlinLogging



val logger = KotlinLogging.logger {}

fun main() {

    val serverConfig = Config.readConfig("config.json")
    //val docRoot = File(serverConfig.server.documentRoot).toPath()

    val app = Javalin.create { config ->
        config.registerPlugin(getConfiguredOpenApiPlugin())
        config.defaultContentType = "application/json"
        config.maxRequestSize = 10_000_000L
    }.apply {
        error(404) { ctx -> ctx.json("Not found") }
    }.start(serverConfig.server.httpPort)
    logger.info { "Server Started!" }

    app.routes {
        path("/images") {
            get(ImageHandler::getAll)
            post(ImageHandler::create)
            path("{imageId}") {
                get(ImageHandler::getImage)
                delete(ImageHandler::deleteImage)
            }
        }
    }
}

fun getConfiguredOpenApiPlugin() = OpenApiPlugin(
    OpenApiOptions {
        openapiDsl {
            info {
                title = "CapVisAPI"
                description = "Used to get and store Images of CapVisAR"
                version = "1.0.0"
            }
        }
    }.apply {
        path("/swagger-docs") // endpoint for OpenAPI json
        swagger(SwaggerOptions("/swagger-ui").title("My Swagger Documentation")) // endpoint for swagger-ui
        reDoc(ReDocOptions("/redoc")) // endpoint for redoc
        defaultDocumentation { doc ->
            doc.json("500", ErrorResponse::class.java)
            doc.json("503", ErrorResponse::class.java)
        }
    }
)
