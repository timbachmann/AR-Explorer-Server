package de.timbachmann.arexplorerserver.database

import kotlinx.serialization.Serializable

/**
 * Configuration of the REST endpoint.
 *
 * @property documentRoot The document root path as a string.
 * @property httpPort The port of the endpoint.
 * @property httpPort The port of the https endpoint.
 * @property enableSsl if ssl / https should be enabled
 * @property keystorePassword password for the keystore
 * @property keystorePath path for the keystore
 */
@Serializable
data class WebServerConfig(val documentRoot: String, val httpPort: Int, val httpsPort: Int, val enableSsl: Boolean, val keystorePath: String, val keystorePassword: String)
