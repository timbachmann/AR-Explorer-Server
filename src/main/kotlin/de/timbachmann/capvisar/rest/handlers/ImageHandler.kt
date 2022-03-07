package de.timbachmann.capvisar.rest.handlers

import de.timbachmann.capvisar.database.ImageDao
import de.timbachmann.capvisar.model.api.response.ErrorResponse
import de.timbachmann.capvisar.model.image.Image
import de.timbachmann.capvisar.model.api.request.NewImageRequest
import de.timbachmann.capvisar.model.api.response.ImageListResponse
import io.javalin.http.Context
import io.javalin.plugin.openapi.annotations.OpenApi
import io.javalin.plugin.openapi.annotations.OpenApiContent
import io.javalin.plugin.openapi.annotations.OpenApiRequestBody
import io.javalin.plugin.openapi.annotations.OpenApiResponse

object ImageHandler {

    private val imageDao: ImageDao = ImageDao()

    @OpenApi(
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
        imageDao.save(name = image.name, data = image.data, lat = image.lat, lng = image.lng, bearing = image.bearing, date = image.date, source = image.source)
        ctx.status(201)
    }

    @OpenApi(
        summary = "Get all images",
        operationId = "getAllImages",
        tags = ["Image"],
        responses = [OpenApiResponse("200", [OpenApiContent(ImageListResponse::class)])]
    )
    fun getAll(ctx: Context) {
        ctx.json(imageDao.getList())
    }
}