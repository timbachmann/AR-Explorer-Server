package de.timbachmann.capvisar.model.image

@kotlinx.serialization.Serializable
data class MetaData(
    var id: String,
    var lat: Double,
    var lng: Double,
    var date: String,
    var source: String,
    var bearing: Int)
