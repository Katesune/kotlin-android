package com.bignerdranch.android.photogallery

import android.app.Application
import androidx.lifecycle.*


class PhotoGalleryViewModel(private val app: Application): AndroidViewModel(app) {

    val galleryItemLiveData: LiveData<List<GalleryItem>>

    private val photoFetchr = PhotoFetchr()
    private val mutableSearchTerm = MutableLiveData<String>()

    val searchTerm: String
        get() = mutableSearchTerm.value ?: ""

    init {
        mutableSearchTerm.value = QueryPreferences.getStoredQuery(app)
        galleryItemLiveData =
                Transformations.switchMap(mutableSearchTerm) { searchTerm ->
                    if (searchTerm.isBlank()) {
                        photoFetchr.fetchPhotos()
                    } else photoFetchr.searchPhotos(searchTerm)
                }
    }

    fun fetchPhotos(query: String = "") {
        QueryPreferences.setStoredQuery(app, query)
        mutableSearchTerm.value = query
    }
}
