package com.bignerdranch.android.photogallery

import com.bignerdranch.android.photogallery.api.PhotoResponse
import com.google.gson.*
import java.lang.reflect.Type

class PhotoDeserializer: JsonDeserializer<PhotoResponse> {

    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): PhotoResponse {

        val galleryItems: List<GalleryItem> = json?.let {
            val jsonPhotos = it.asJsonObject.get("photos").asJsonObject
            parseGalleryItems(jsonPhotos)
        } ?: listOf()

        return PhotoResponse().apply {
            this.galleryItems = galleryItems
        }
    }

    private val parseGalleryItems: (JsonObject) -> List<GalleryItem> = { jsonObject ->
        jsonObject.getAsJsonArray("photo").map { photo ->
            GalleryItem(
                    photo.asJsonObject.get("title").asString,
                    photo.asJsonObject.get("id").asString,
                    photo.asJsonObject.get("url_s").asString,
                    photo.asJsonObject.get("owner").asString
            )
        }.filterNot { it.url.isBlank() }
    }

}

