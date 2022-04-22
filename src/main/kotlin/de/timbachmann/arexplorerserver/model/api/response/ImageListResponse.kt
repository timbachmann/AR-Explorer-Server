package de.timbachmann.arexplorerserver.model.api.response

import de.timbachmann.arexplorerserver.model.image.ApiImage

data class ImageListResponse (val apiImages: Array<ApiImage>) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ImageListResponse

        if (!apiImages.contentEquals(other.apiImages)) return false

        return true
    }

    override fun hashCode(): Int {
        return apiImages.contentHashCode()
    }
}
