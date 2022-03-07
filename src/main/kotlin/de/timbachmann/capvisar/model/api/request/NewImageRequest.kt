package de.timbachmann.capvisar.model.api.request

data class NewImageRequest(
    var name: String,
    var data: ByteArray,
    var lat: Double,
    var lng: Double,
    var date: String,
    var source: String,
    var bearing: Int )