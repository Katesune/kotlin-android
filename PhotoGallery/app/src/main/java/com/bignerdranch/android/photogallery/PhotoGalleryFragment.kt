package com.bignerdranch.android.photogallery

import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.work.*
import java.util.concurrent.TimeUnit


private const val TAG = "PhotoGalleryFragment"
private const val POLL_WORK = "POLL_WORK"

class PhotoGalleryFragment: VisibleFragment() {

    private lateinit var photoGalleryViewModel: PhotoGalleryViewModel
    private lateinit var photoRecyclerView: RecyclerView
    private lateinit var thumbnailDownloader: ThumbnailDownloader<PhotoHolder>
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setHasOptionsMenu(true)
        photoGalleryViewModel = ViewModelProviders.of(this).get(PhotoGalleryViewModel::class.java)

        val responseHandler = Handler()

        thumbnailDownloader = ThumbnailDownloader(responseHandler) { photoHolder, bitmap ->
            val drawable = BitmapDrawable(resources, bitmap)
            photoHolder.bindDrawable(drawable)
        }

        lifecycle.addObserver(thumbnailDownloader.fragmentLifecycleObserver)

        viewLifecycleOwnerLiveData.observe(
                this,
                { viewLifecycleOwner.lifecycle.addObserver(thumbnailDownloader.viewLifecycleObserver) }
        )
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_photo_gallery, container, false)
        progressBar = view.findViewById(R.id.progressBar) as ProgressBar

        photoRecyclerView = view.findViewById(R.id.photo_recycler_view)
        photoRecyclerView.viewTreeObserver.addOnGlobalLayoutListener(PhotoLayoutListener())

        return view
    }

    private inner class PhotoLayoutListener: ViewTreeObserver.OnGlobalLayoutListener {
        override fun onGlobalLayout() {
            photoRecyclerView.viewTreeObserver.removeGlobalOnLayoutListener(this)
            val width = photoRecyclerView.width
            val spanCount = (width / 360)
            photoRecyclerView.layoutManager = GridLayoutManager(context, spanCount)
        }

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        photoGalleryViewModel.galleryItemLiveData.observe(
                viewLifecycleOwner,
                Observer { galleryItems ->
                    photoRecyclerView.adapter = PhotoAdapter(galleryItems)
                    progressBar.visibility = View.INVISIBLE
                    photoRecyclerView.visibility = View.VISIBLE
                }
        )
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.fragment_photo_gallery, menu)

        val searchItem: MenuItem = menu.findItem(R.id.menu_item_search)
        val searchView = searchItem.actionView as SearchView

        searchView.apply {
            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(queryText: String): Boolean {
                    Log.d(TAG, "QueryTextSubmit: $queryText")
                    searchView.clearFocus()
                    photoRecyclerView.visibility = View.INVISIBLE
                    progressBar.visibility = View.VISIBLE

                    photoGalleryViewModel.fetchPhotos(queryText)
                    return true
                }

                override fun onQueryTextChange(queryText: String): Boolean {
                    Log.d(TAG, "QueryTextChange: $queryText")
                    return false
                }

            })

            setOnSearchClickListener {
                searchView.setQuery(photoGalleryViewModel.searchTerm, false)
            }

            val toggleItem = menu.findItem(R.id.menu_item_toggle_polling)
            val isPolling = QueryPreferences.isPolling(requireContext())
            val toggleItemTitle = if (isPolling) {
                R.string.stop_polling
            } else R.string.start_polling
            toggleItem.setTitle(toggleItemTitle)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_item_clear -> {
                photoGalleryViewModel.fetchPhotos("")
                true
            }
            R.id.menu_item_toggle_polling -> {
                val isPolling = QueryPreferences.isPolling(requireContext())

                if (isPolling) {
                    WorkManager.getInstance().cancelUniqueWork(POLL_WORK)
                    QueryPreferences.setPolling(requireContext(), false)

                } else {
                    val constraints = Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.UNMETERED)
                        .build()
                    val periodicRequest = PeriodicWorkRequest
                        .Builder(PollWorker::class.java, 15, TimeUnit.MINUTES)
                        .setConstraints(constraints)
                        .build()
                    WorkManager.getInstance().enqueueUniquePeriodicWork(POLL_WORK,
                    ExistingPeriodicWorkPolicy.KEEP,
                    periodicRequest)
                    QueryPreferences.setPolling(requireContext(), true)
                }
                activity?.invalidateOptionsMenu()
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private inner class PhotoHolder(private val itemImageView: ImageView):
        RecyclerView.ViewHolder(itemImageView), View.OnClickListener {

        private lateinit var galleryItem: GalleryItem

        init {
            itemView.setOnClickListener(this)
        }

        val bindDrawable: (Drawable) -> Unit = itemImageView::setImageDrawable

        fun bindGalleryItem(item: GalleryItem) {
            galleryItem = item
        }

        override fun onClick(view: View) {
            val intent = PhotoPageActivity
                .newIntent(requireContext(), galleryItem.photoPageUri)
            startActivity(intent)
//            CustomTabsIntent.Builder()
//                .setToolbarColor(ContextCompat.getColor(requireContext(), R.color.design_default_color_primary))
//                .setShowTitle(true)
//                .build()
//                .launchUrl(requireContext(), galleryItem.photoPageUri)
        }
    }

    companion object {
        fun newInstance() = PhotoGalleryFragment()
    }

    private inner class PhotoAdapter(private val galleryItems: List<GalleryItem>):
            RecyclerView.Adapter<PhotoHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoHolder {
            val view = layoutInflater.inflate(
                    R.layout.list_item_gallery,
                    parent,
                    false
            ) as ImageView
            return PhotoHolder(view)
        }

        override fun getItemCount(): Int = galleryItems.size

        override fun onBindViewHolder(holder: PhotoHolder, position: Int) {
            val galleryItem = galleryItems[position]
            holder.bindGalleryItem(galleryItems[position])

            val placeholder: Drawable = ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.bill_up_close
            ) ?: ColorDrawable()
            holder.bindDrawable(placeholder)

            val rangeByPosition: () -> List<Int> = {
                (position - 10..position + 10).toList().filter {it in 1 until itemCount && it != position}
            }

            for (i in rangeByPosition()) {
                galleryItems[i].let {
                    thumbnailDownloader.queueThumbnail(holder, it.url)
                }
            }

            thumbnailDownloader.queueThumbnail(holder, galleryItem.url)

        }
    }

}