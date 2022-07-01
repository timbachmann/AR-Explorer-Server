package de.timbachmann.arexplorerserver.rest.handlers

import de.timbachmann.arexplorerserver.database.ImageDao
import de.timbachmann.arexplorerserver.model.api.response.ErrorResponse
import de.timbachmann.arexplorerserver.model.api.request.NewImageRequest
import de.timbachmann.arexplorerserver.model.api.response.ImageListResponse
import de.timbachmann.arexplorerserver.model.image.ApiImage
import io.javalin.http.Context
import io.javalin.plugin.openapi.annotations.*

object ImageHandler {

    private val imageDao: ImageDao = ImageDao()

    @OpenApi(
        path = "/images",
        method = HttpMethod.POST,
        summary = "Create new image",
        operationId = "createImage",
        tags = ["Image"],
        requestBody = OpenApiRequestBody([OpenApiContent(NewImageRequest::class)]),
        responses = [
            OpenApiResponse("201"),
            OpenApiResponse("400", [OpenApiContent(ErrorResponse::class)])
        ]
    )
    fun create(ctx: Context) {
        val image = ctx.bodyAsClass<NewImageRequest>()
        imageDao.save(userID = image.userID, id = image.id, data = image.data, lat = image.lat, lng = image.lng, bearing = image.bearing, date = image.date, source = image.source, yaw = image.yaw, pitch = image.pitch, isPublic = image.publicImage)
        ctx.status(201)
    }

    @OpenApi(
        path = "/images",
        method = HttpMethod.GET,
        summary = "Get all images with filter",
        operationId = "getAllImagesWithFilter",
        tags = ["Image"],
        queryParams = [
            OpenApiParam("userID", String::class, description = "user ID", required = true),
            OpenApiParam("startDate", String::class, description = "start date for temporal filter", required = true),
            OpenApiParam("endDate", String::class, description = "end date for temporal filter", required = true),
            OpenApiParam("lat", String::class, description = "latitude for spatial filter", required = true),
            OpenApiParam("lng", String::class, description = "longitude for spatial filter", required = true),
            OpenApiParam("radius", String::class, description = "radius for spatial filter", required = true),
            OpenApiParam("includePublic", String::class, description = "include public images or not", required = true)],
        responses = [OpenApiResponse("200", [OpenApiContent(ImageListResponse::class)])]
    )
    fun getAllWithFilter(ctx: Context) {
        imageDao.getListWithFilter(
            ctx.queryParam("userID").orEmpty(),
            ctx.queryParam("startDate").orEmpty(),
            ctx.queryParam("endDate").orEmpty(),
            ctx.queryParamAsClass<Double>("lat").getOrDefault(0.0),
            ctx.queryParamAsClass<Double>("lng").getOrDefault(0.0),
            ctx.queryParamAsClass<Int>("radius").getOrDefault(0),
            ctx.queryParam("includePublic").orEmpty())
            .let { ctx.json(it) }
    }

    @OpenApi(
        path = "/images/{userID}/{imageId}",
        method = HttpMethod.GET,
        summary = "Get image by id",
        operationId = "getImageById",
        tags = ["Image"],
        pathParams = [
            OpenApiParam("userID", String::class, description = "user ID", required = true),
            OpenApiParam("imageId", String::class, description = "id to search for", required = true)],
        responses = [OpenApiResponse("200", [OpenApiContent(ApiImage::class)])]
    )
    fun getImage(ctx: Context) {
        imageDao.getApiImageById(ctx.pathParam("imageId"), ctx.pathParam("userID"))?.let { ctx.json(it) }
    }

    @OpenApi(
        path = "/images/{userID}/{imageId}",
        method = HttpMethod.DELETE,
        summary = "Delete image by id",
        operationId = "deleteImageById",
        tags = ["Image"],
        pathParams = [
            OpenApiParam("userID", String::class, description = "user ID", required = true),
            OpenApiParam("imageId", String::class, description = "id to search for", required = true)],
        responses = [OpenApiResponse("200")]
    )
    fun deleteImage(ctx: Context) {
        imageDao.deleteApiImageById(ctx.pathParam("imageId"), ctx.pathParam("userID"))
        ctx.status(200)
    }

    @OpenApi(
        path = "/images/{userID}/{imageId}",
        method = HttpMethod.PUT,
        summary = "Update image by id",
        operationId = "updateImageById",
        tags = ["Image"],
        pathParams = [
            OpenApiParam("userID", String::class, description = "user ID", required = true),
            OpenApiParam("imageId", String::class, description = "id to search for", required = true)],
        responses = [OpenApiResponse("200")]
    )
    fun updateImage(ctx: Context) {
        imageDao.updateApiImageById(ctx.pathParam("imageId"), ctx.pathParam("userID"))
        ctx.status(200)
    }
}