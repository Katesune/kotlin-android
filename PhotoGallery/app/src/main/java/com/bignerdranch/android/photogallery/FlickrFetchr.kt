package com.bignerdranch.android.photogallery

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.bignerdranch.android.photogallery.api.FlickrApi
import com.bignerdranch.android.photogallery.api.FlickrResponse
import com.bignerdranch.android.photogallery.api.PhotoResponse
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializer
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import androidx.paging.PagingData
import androidx.paging.PagingSource

private const val TAG = "FlickrFetchr"

class PhotoFetchr {

    private val flickrApi: FlickrApi

    init {
        val gson = GsonBuilder()
            .registerTypeAdapter(PhotoResponse::class.java, PhotoDeserializer())
            .create()

        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl("https://api.flickr.com/")
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

        flickrApi = retrofit.create(FlickrApi::class.java)
    }

    fun fetchPhotos(): LiveData<List<GalleryItem>> {
        val responseLiveData: MutableLiveData<List<GalleryItem>> = MutableLiveData()
        val flickrRequest: Call<PhotoResponse> = flickrApi.fetchPhotos()

        flickrRequest.enqueue(object: Callback<PhotoResponse> {

            override fun onFailure(call: Call<PhotoResponse>, t: Throwable) {
                Log.e(TAG, "Failed to fetch photos", t)
            }

            override fun onResponse(call: Call<PhotoResponse>, response: Response<PhotoResponse>) {
                Log.d(TAG, "Response received: ${response.body()}")

                val photoResponse: PhotoResponse? = response.body()
                val galleryItems: List<GalleryItem> = photoResponse?.galleryItems ?: mutableListOf()
                responseLiveData.value = galleryItems
            }
        })

        return responseLiveData
    }
}