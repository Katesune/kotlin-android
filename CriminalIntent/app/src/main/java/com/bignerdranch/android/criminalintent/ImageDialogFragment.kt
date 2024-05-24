package com.bignerdranch.android.criminalintent

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.DialogFragment

private const val ARG_PATH = "image_path"

class EnlargedPhotoFragment(): DialogFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.photo_dialog, container, false)

        val photoView = view.findViewById(R.id.enlarged_photo) as ImageView
        val photoPath = arguments?.getSerializable(ARG_PATH) as String

        val bitmap = getScaledBitmap(photoPath, requireActivity())
        photoView.setImageBitmap(bitmap)

        return view
    }

    companion object {
        fun newInstance(path: String): EnlargedPhotoFragment {
            val args = Bundle().apply {
                putSerializable(ARG_PATH, path)
            }
            return EnlargedPhotoFragment().apply { arguments = args }
        }

    }
}