package de.timbachmann.arexplorerserver.rest

import cc.vileda.openapi.dsl.info
import cc.vileda.openapi.dsl.openapiDsl
import de.timbachmann.arexplorerserver.database.Config
import de.timbachmann.arexplorerserver.database.WebServerConfig
import de.timbachmann.arexplorerserver.model.api.response.ErrorResponse
import de.timbachmann.arexplorerserver.rest.handlers.ImageHandler
import io.javalin.Javalin
import io.javalin.apibuilder.ApiBuilder.*
import io.javalin.plugin.openapi.OpenApiOptions
import io.javalin.plugin.openapi.OpenApiPlugin
import io.javalin.plugin.openapi.ui.ReDocOptions
import io.javalin.plugin.openapi.ui.SwaggerOptions
import mu.KotlinLogging
import org.eclipse.jetty.server.*


val logger = KotlinLogging.logger {}

fun main() {

    val serverConfig = Config.readConfig("config.json")

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
            get(ImageHandler::getAllWithFilter)
            post(ImageHandler::create)
            path("{userID}") {
                path("{imageId}") {
                    get(ImageHandler::getImage)
                    delete(ImageHandler::deleteImage)
                }
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

/*private fun setupHttpServer(config: WebServerConfig): Server {

    val httpConfig = HttpConfiguration().apply {
        sendServerVersion = false
        sendXPoweredBy = false
        if (config.enableSsl) {
            secureScheme = "https"
            securePort = config.httpsPort
        }
    }

    if (config.enableSsl) {
        val httpsConfig = HttpConfiguration(httpConfig).apply {
            addCustomizer(SecureRequestCustomizer())
        }

        val alpn = ALPNServerConnectionFactory().apply {
            defaultProtocol = "http/1.1"
        }

        val sslContextFactory = SslContextFactory.Server().apply {
            keyStorePath = config.keystorePath
            setKeyStorePassword(config.keystorePassword)
            //cipherComparator = HTTP2Cipher.COMPARATOR
            provider = "Conscrypt"
        }

        val ssl = SslConnectionFactory(sslContextFactory, alpn.protocol)

        val http2 = HTTP2ServerConnectionFactory(httpsConfig)

        val fallback = HttpConnectionFactory(httpsConfig)

        return Server().apply {
            //HTTP Connector
            addConnector(
                ServerConnector(
                    server,
                    HttpConnectionFactory(httpConfig),
                    HTTP2ServerConnectionFactory(httpConfig)
                ).apply {
                    port = config.httpPort
                })
            // HTTPS Connector
            addConnector(ServerConnector(server, ssl, alpn, http2, fallback).apply {
                port = config.httpsPort
            })
        }
    } else {
        return Server().apply {
            //HTTP Connector
            addConnector(
                ServerConnector(
                    server,
                    HttpConnectionFactory(httpConfig),
                    HTTP2ServerConnectionFactory(httpConfig)
                ).apply {
                    port = config.httpPort
                })

        }
    }
}*/
