package com.muchbeer.imageuploaderktx.model


data class ImageResponse(
    val error: Boolean,
    val image: String,
    val message: String
)