package com.bignerdranch.android.photogallery.api

import com.bignerdranch.android.photogallery.GalleryItem
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Url

interface FlickrApi {
    @GET("services/rest/?method=flickr.interestingness.getList" +
    "&api_key=37b92c23dda3a570d3f6e1166471957d" +
    "&format=json" +
    "&nojsoncallback=1" +
    "&extras=url_s")
    fun fetchPhotos(): Call<PhotoResponse>

    @GET
    fun fetchUrlBytes(@Url url: String): Call<ResponseBody>
}
