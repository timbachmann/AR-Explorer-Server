package de.timbachmann.capvisar.rest.handlers

import de.timbachmann.capvisar.database.ImageDao
import de.timbachmann.capvisar.model.api.response.ErrorResponse
import de.timbachmann.capvisar.model.api.request.NewImageRequest
import de.timbachmann.capvisar.model.api.response.ImageListResponse
import de.timbachmann.capvisar.model.filter.Filter
import de.timbachmann.capvisar.model.image.ApiImage
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
        imageDao.save(id = image.id, data = image.data, lat = image.lat, lng = image.lng, bearing = image.bearing, date = image.date, source = image.source)
        ctx.status(201)
    }

    @OpenApi(
        path = "/images",
        method = HttpMethod.GET,
        summary = "Get all images",
        operationId = "getAllImages",
        tags = ["Image"],
        responses = [OpenApiResponse("200", [OpenApiContent(ImageListResponse::class)])]
    )
    fun getAll(ctx: Context) {
        ctx.json(imageDao.getList())
    }

    @OpenApi(
        path = "/images",
        method = HttpMethod.GET,
        summary = "Get all images with filter",
        operationId = "getAllImagesWithFilter",
        tags = ["Image"],
        requestBody = OpenApiRequestBody([OpenApiContent(Filter::class)]),
        responses = [OpenApiResponse("200", [OpenApiContent(ImageListResponse::class)])]
    )
    fun getAllWithFilter(ctx: Context) {
        val filter = ctx.bodyAsClass<Filter>()
        ctx.json(imageDao.getListWithFilter(filter))
    }

    @OpenApi(
        path = "/images/{imageId}",
        method = HttpMethod.GET,
        summary = "Get image by id",
        operationId = "getImageById",
        tags = ["Image"],
        pathParams = [OpenApiParam("imageId", String::class, description = "id to search for", required = true)],
        responses = [OpenApiResponse("200", [OpenApiContent(ApiImage::class)])]
    )
    fun getImage(ctx: Context) {
        imageDao.getApiImageById(ctx.pathParam("imageId"))?.let { ctx.json(it) }
    }

    @OpenApi(
        path = "/images/{imageId}",
        method = HttpMethod.DELETE,
        summary = "Delete image by id",
        operationId = "deleteImageById",
        tags = ["Image"],
        pathParams = [OpenApiParam("imageId", String::class, description = "id to search for", required = true)],
        responses = [OpenApiResponse("200", [OpenApiContent(ApiImage::class)])]
    )
    fun deleteImage(ctx: Context) {
        imageDao.deleteApiImageById(ctx.pathParam("imageId")).let { ctx.json(it) }
    }
}