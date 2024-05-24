package com.bignerdranch.android.criminalintent

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.text.Editable
import android.text.TextWatcher
import android.text.format.DateFormat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import java.util.*
import androidx.lifecycle.Observer
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.Manifest
import android.provider.MediaStore
import android.widget.*
import androidx.core.content.FileProvider
import java.io.File


private const val TAG = "CrimeFragment"
private const val ARG_CRIME_ID = "crime_id"
private const val DIALOG_DATE = "DialogDate"
private const val DIALOG_TIME = "DialogTime"
private const val DIALOG_ENLARGED_PHOTO = "DialogPhoto"
private const val REQUEST_DATE = 0
private const val REQUEST_TIME = 1
private const val REQUEST_CONTACT = 1
private const val REQUEST_PHONE_NUMBER = 2
private const val REQUEST_PHOTO = 3
private const val DATE_FORMAT = "EEE, MMM, dd"

class CrimeFragment: Fragment(),
        DatePickerFragment.Callbacks, TimePickerFragment.Callbacks {

    private lateinit var crime: Crime
    private lateinit var photoFile: File
    private lateinit var photoUri: Uri

    private lateinit var titleField:EditText
    private lateinit var dateButton: Button
    private lateinit var timeButton: Button
    private lateinit var solvedCheckBox: CheckBox
    private lateinit var reportButton: Button
    private lateinit var suspectButton: Button
    private lateinit var suspectCallButton: Button
    private lateinit var photoButton: ImageButton
    private lateinit var photoView: ImageView

    private val crimeDetailViewModel: CrimeDetailViewModel by lazy {
        ViewModelProviders.of(this).get(CrimeDetailViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        crime = Crime()
        val crimeId: UUID = arguments?.getSerializable(ARG_CRIME_ID) as UUID
        crimeDetailViewModel.loadCrime(crimeId)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_crime, container, false)

        titleField = view.findViewById(R.id.crime_title) as EditText
        dateButton = view.findViewById(R.id.crime_date) as Button
        timeButton = view.findViewById(R.id.crime_time) as Button
        solvedCheckBox = view.findViewById(R.id.crime_solved) as CheckBox
        reportButton = view.findViewById(R.id.crime_report) as Button
        suspectButton = view.findViewById(R.id.crime_suspect) as Button
        suspectCallButton = view.findViewById(R.id.crime_suspect_call) as Button
        photoButton = view.findViewById(R.id.crime_camera) as ImageButton
        photoView = view.findViewById(R.id.crime_photo) as ImageView

        photoView.viewTreeObserver.addOnGlobalLayoutListener(PhotoObserver())

        return view
    }

    inner class PhotoObserver: OnGlobalLayoutListener {
        override fun onGlobalLayout() {
            if (photoView.isActivated) {
                updatePhotoView()
            }
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        crimeDetailViewModel.crimeLiveData.observe(
            viewLifecycleOwner,
            Observer { crime ->
                crime?.let {
                    this.crime = crime
                    photoFile = crimeDetailViewModel.getPhotoFile(crime)
                    photoUri = FileProvider.getUriForFile(
                            requireActivity(),
                            "com.bignerdranch.android.criminalintent.fileprovider",
                            photoFile
                    )
                    updateUI()
                }
            })
    }

    override fun onStart() {
        super.onStart()

        val titleWatcher = object: TextWatcher {

            override fun beforeTextChanged(
                sequence: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {
                //
            }

            override fun onTextChanged(
                sequence: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
                crime.title = sequence.toString()
            }

            override fun afterTextChanged(sequence: Editable?) {
                //s
            }
        }

        titleField.addTextChangedListener(titleWatcher)

        solvedCheckBox.apply {
            setOnCheckedChangeListener { _, isChecked ->
                crime.isSolved = isChecked
            }
        }

        dateButton.setOnClickListener {
            DatePickerFragment.newInstance(crime.date).apply {
                setTargetFragment(this@CrimeFragment, REQUEST_DATE)
                show(this@CrimeFragment.requireFragmentManager(), DIALOG_DATE)
            }
        }

        timeButton.setOnClickListener {
            TimePickerFragment.newInstance(crime.date).apply {
                setTargetFragment(this@CrimeFragment, REQUEST_TIME)
                show(this@CrimeFragment.requireFragmentManager(), DIALOG_TIME)
            }
        }

        reportButton.setOnClickListener {
            Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, getCrimeReport())
                putExtra(
                        Intent.EXTRA_SUBJECT,
                        getString(R.string.crime_report_subject))
            }.also { intent ->
                val chooserIntent = Intent.createChooser(intent, getString(R.string.send_report))
                startActivity(chooserIntent)
            }
        }

        suspectButton.apply {
            val pickContactIntent =
                    Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI)

            setOnClickListener {
                startActivityForResult(pickContactIntent, REQUEST_CONTACT)
            }

            val packageManager: PackageManager = requireActivity().packageManager
            val resolvedActivity: ResolveInfo? =
                    packageManager.resolveActivity(pickContactIntent,
                    PackageManager.MATCH_DEFAULT_ONLY)
            if (resolvedActivity == null) {
                isEnabled = false
                suspectCallButton.isEnabled = false
            }
        }

        suspectCallButton.apply {

            setOnClickListener {
                Manifest.permission.READ_CONTACTS.apply {
                    when {
                        context.checkSelfPermission(this) != PackageManager.PERMISSION_GRANTED -> {
                            requestPermissions(arrayOf(this), REQUEST_PHONE_NUMBER)
                        }
                        crime.suspect.isNotEmpty() -> startSuspendCallIntent()
                        else -> suspectCallButton.text = getString(R.string.no_suspend_for_call)
                    }
                }

            }
        }

        photoButton.apply {
            val packageManager: PackageManager = requireActivity().packageManager

            val captureImage = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            val resolvedActivity: ResolveInfo? =
                    packageManager.resolveActivity(
                            captureImage,
                            PackageManager.MATCH_DEFAULT_ONLY)

            if (resolvedActivity == null) isEnabled = false

            setOnClickListener {
                Log.d(TAG, "image")
                captureImage.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)

                val cameraActivities: List<ResolveInfo> =
                        packageManager.queryIntentActivities(
                                captureImage,
                                PackageManager.MATCH_DEFAULT_ONLY)

                for (cameraActivity in cameraActivities) {
                    requireActivity().grantUriPermission(
                            cameraActivity.activityInfo.packageName,
                            photoUri,
                            Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    )
                }

                startActivityForResult(captureImage, REQUEST_PHOTO)
            }
        }

        photoView.apply {
            setOnClickListener {
                if (!photoFile.exists()) isEnabled = false

                EnlargedPhotoFragment.newInstance(photoFile.path).apply {
                    show(this@CrimeFragment.requireFragmentManager(), DIALOG_ENLARGED_PHOTO)
                }

            }
        }

    }

    private fun startSuspendCallIntent() {
        val callContactIntent =
                Intent(Intent.ACTION_DIAL)

        val suspectPhone = getSuspendPhone()
        callContactIntent.data = Uri.parse("tel:$suspectPhone")

        startActivityForResult(callContactIntent, REQUEST_PHONE_NUMBER)
    }

    private fun getSuspendPhone(): String {
        val suspectId = oneFieldQuery (
                ContactsContract.Contacts.CONTENT_URI,
                ContactsContract.Contacts._ID,
                ContactsContract.Contacts.DISPLAY_NAME +" = '${crime.suspect}'")

        return oneFieldQuery(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                ContactsContract.CommonDataKinds.Phone.NUMBER,
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = '$suspectId'"
        )
    }

    override fun onDetach() {
        super.onDetach()
        requireActivity().revokeUriPermission(
                photoUri,
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        )
    }

    override fun onStop() {
        super.onStop()
        crimeDetailViewModel.saveCrime(crime)
    }

    override fun onDateSelected(date: Date) {
        crime.date = date
        updateUI()
    }

    override fun onTimeSelected(date: Date) {
        crime.date = date
        updateUI()
    }

    private fun updateUI() {
        titleField.setText(crime.title)
        dateButton.text = dateConverted()
        timeButton.text = timeConverted()
        solvedCheckBox.apply {
            isChecked = crime.isSolved
            jumpDrawablesToCurrentState()
        }
        if (crime.suspect.isNotEmpty()) {
            suspectButton.text = crime.suspect
        }
        updatePhotoView()
    }

    private fun updatePhotoView() {
        if (photoFile.exists()) {
            val bitmap = getScaledBitmap(photoFile.path, requireActivity())
            photoView.setImageBitmap(bitmap)
            photoView.contentDescription = getString(R.string.crime_photo_image_description)
        } else {
            photoButton.setImageDrawable(null)
            photoView.contentDescription = getString(R.string.crime_photo_no_image_description)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Log.d(TAG, "onActivityResult")
        when {
            resultCode != Activity.RESULT_OK -> return

            requestCode == REQUEST_CONTACT && data != null -> {
                val contactUri: Uri? = data.data
                val queryFields = arrayOf((ContactsContract.Contacts.DISPLAY_NAME))

                val cursor = contactUri?.let {
                    requireActivity().contentResolver
                            .query(it, queryFields, null, null, null)
                }

                cursor?.use {
                    if (it.count == 0) {
                        return
                    }

                    it.moveToFirst()
                    val suspect = it.getString(0)
                    crime.suspect = suspect
                    crimeDetailViewModel.saveCrime(crime)
                    suspectButton.text = suspect
                }
            }

            requestCode == REQUEST_PHONE_NUMBER -> {
                if (crime.suspect.isEmpty()) suspectCallButton.text = getString(R.string.no_suspend_for_call)
                else suspectCallButton.text = getString(R.string.crime_report_call_text)
            }

            requestCode == REQUEST_PHOTO -> {
                requireActivity().revokeUriPermission(
                        photoUri,
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )
                updatePhotoView()
            }
        }
    }

    private fun getCrimeReport(): String {
        val solvedString = if (crime.isSolved) {
            getString(R.string.crime_report_solved)
        } else {
            getString(R.string.crime_report_unsolved)
        }

        val dateString = DateFormat.format(DATE_FORMAT, crime.date).toString()
        var suspect = if (crime.suspect.isBlank()) {
            getString(R.string.crime_report_no_suspect)
        } else {
            getString(R.string.crime_report_suspect, crime.suspect)
        }

        return getString(R.string.crime_report,
            crime.title, dateString, solvedString, suspect)
    }

    val dateConverted: () -> String = { ->
        DateFormat.format("EEEE, MMMM dd, yyyy", crime.date).toString()
    }

    val timeConverted: () -> String = {
        DateFormat.format("HH mm", crime.date).toString()
    }

    private fun oneFieldQuery(uri: Uri?, initQueryFields: String, query: String): String {
        val queryFields = arrayOf(initQueryFields)
        val cursor = uri?.let {
            requireActivity().contentResolver
                    .query(it, queryFields,
                            query,
                            null,
                            null)
        }

        cursor?.use {
            if (it.count == 0) {
                return ""
            }

            it.moveToFirst()
            return it.getString(0)
        }
        return ""
    }

    companion object {

        fun newInstance(crimeID: UUID): CrimeFragment {
            val args = Bundle().apply {
                putSerializable(ARG_CRIME_ID, crimeID)
            }
            return CrimeFragment().apply {
                arguments = args
            }
        }
    }


}