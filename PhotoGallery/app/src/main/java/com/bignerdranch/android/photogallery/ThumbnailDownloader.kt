package com.bignerdranch.android.photogallery

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Handler
import android.os.HandlerThread
import android.os.Message
import android.util.Log
import android.util.LruCache
import androidx.lifecycle.*
import java.util.concurrent.ConcurrentHashMap

private const val TAG = "ThumbnailDownloader"
private const val MESSAGE_DOWNLOAD = 0

class ThumbnailDownloader<in T>(
        private val responseHandler: Handler,
        private val onThumbnailDownloaded: (T, Bitmap) -> Unit
): HandlerThread(TAG) {

    val fragmentLifecycleObserver: DefaultLifecycleObserver =
            object: DefaultLifecycleObserver {

                override fun onCreate(owner: LifecycleOwner) {
                    super.onCreate(owner)
                    Log.i(TAG, "Starting background thread")
                    start()
                    looper
                }

                override fun onStop(owner: LifecycleOwner) {
                    super.onStop(owner)
                    Log.i(TAG, "Destroying background thread")
                    quit()
                    owner.lifecycle.removeObserver(this)
                }
            }

    val viewLifecycleObserver: DefaultLifecycleObserver =
            object: DefaultLifecycleObserver {
                override fun onStop(owner: LifecycleOwner) {
                    super.onStop(owner)
                    Log.i(TAG, "Clearing all requests from queue")
                    requestHandler.removeMessages(MESSAGE_DOWNLOAD)
                    requestMap.clear()
                    owner.lifecycle.removeObserver(this)
                }
            }

    private var hasQuit = false
    private lateinit var requestHandler: Handler
    private val requestMap = ConcurrentHashMap<T, String>()
    private val photoFetchr = PhotoFetchr()

    @Suppress("UNCHECKED_CAST")
    @SuppressLint("HandlerLeak")
    override fun onLooperPrepared() {
        requestHandler = object: Handler() {
            override fun handleMessage(msg: Message) {
                if (msg.what == MESSAGE_DOWNLOAD) {
                    val target = msg.obj as T
                    Log.i(TAG, "got a request for URL: ${requestMap[target]}")

                    handleRequest(target)
                }
            }
        }
    }

    override fun quit(): Boolean {
        hasQuit = true
        Log.d(TAG, "quit")
        return super.quit()
    }

    fun queueThumbnail(target: T, url: String)  {
        //Log.d(TAG, "Got a URL: $url")
        requestMap[target] = url
        requestHandler.obtainMessage(MESSAGE_DOWNLOAD, target)
            .sendToTarget()
    }

    private val cacheSize = Runtime.getRuntime().freeMemory()

    private val imageCache = object: LruCache<String, Bitmap>(cacheSize.toInt()) {
        override fun sizeOf(key: String?, value: Bitmap?): Int {
            return value?.byteCount?.div(1024) ?: 0
        }
    }

    private fun handleRequest(target: T) {
        val url = requestMap[target] ?: return

        val bitmap = if (imageCache.get(url) == null) {
            photoFetchr.fetchPhoto(url).apply {
                imageCache.put(url, this)
            } ?: return
        } else imageCache.get(url)

        responseHandler.post(Runnable {
            if (requestMap[target] != url || hasQuit) {
                return@Runnable
            }

            requestMap.remove(target)
            onThumbnailDownloaded(target, bitmap)
        })
    }

}