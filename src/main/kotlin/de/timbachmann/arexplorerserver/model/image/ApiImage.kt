package de.timbachmann.arexplorerserver.model.image

@kotlinx.serialization.Serializable
data class ApiImage(
    var id: String,
    var data: ByteArray,
    var thumbnail: ByteArray,
    var lat: Double,
    var lng: Double,
    var date: String,
    var source: String,
    var bearing: Int,
    var yaw: Float,
    var pitch: Float) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ApiImage

        if (id != other.id) return false
        if (!data.contentEquals(other.data)) return false
        if (lat != other.lat) return false
        if (lng != other.lng) return false
        if (date != other.date) return false
        if (source != other.source) return false
        if (bearing != other.bearing) return false
        if (yaw != other.yaw) return false
        if (pitch != other.pitch) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + data.contentHashCode()
        result = 31 * result + lat.hashCode()
        result = 31 * result + lng.hashCode()
        result = 31 * result + date.hashCode()
        result = 31 * result + source.hashCode()
        result = 31 * result + bearing
        result = 31 * result + yaw.hashCode()
        result = 31 * result + pitch.hashCode()
        return result
    }
}