package com.bignerdranch.android.beatbox

import android.content.res.AssetManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class BeatBoxModelFactory (private val assets: AssetManager): ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return BeatBoxViewModel(assets) as T
    }
}

class BeatBoxViewModel(assets: AssetManager): ViewModel() {

    var beatBox = BeatBox(assets)
}