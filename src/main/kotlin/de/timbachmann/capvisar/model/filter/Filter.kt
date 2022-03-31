package de.timbachmann.capvisar.model.filter

data class Filter(
    var startDate: String?,
    var endDate: String?,
    var lat: Double?,
    var lng: Double?,
    var radius: Int?
)
