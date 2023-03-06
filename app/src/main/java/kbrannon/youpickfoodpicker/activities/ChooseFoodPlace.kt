package kbrannon.youpickfoodpicker.activities

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.ActivityNotFoundException
import android.content.ContentValues.TAG
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.Autocomplete.getPlaceFromIntent
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import kbrannon.youpickfoodpicker.R
import kbrannon.youpickfoodpicker.database.DatabaseHandler
import kbrannon.youpickfoodpicker.models.YouPickFoodPickerModel
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.Exception
import android.widget.Toast
import com.google.android.gms.location.*
import com.google.android.libraries.places.widget.AutocompleteActivity
import kbrannon.youpickfoodpicker.databinding.ActivityChooseFoodPlaceBinding
import kbrannon.youpickfoodpicker.utils.GetAddressFromLatLng

class ChooseFoodPlace : AppCompatActivity(), View.OnClickListener {

    private var cal = Calendar.getInstance()
    private lateinit var dateSetListener: DatePickerDialog.OnDateSetListener
    private var timesRequestedPermission = 0
    private var saveImageToInternalStorageDevice: Uri? = null
    private var mLatitude: Double = 0.0
    private var mLongitude: Double = 0.0
    private var mFoodPlaceDetails: YouPickFoodPickerModel? = null

    private lateinit var mFusedLocationProviderClient: FusedLocationProviderClient

    private var binding: ActivityChooseFoodPlaceBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityChooseFoodPlaceBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding?.root)
        setSupportActionBar(binding?.toolbarAddPlace)

        if(supportActionBar != null){
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
        }

        binding?.toolbarAddPlace?.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        if(!Places.isInitialized()){
            Places.initialize(this@ChooseFoodPlace, resources.getString(R.string.google_maps_key))
        }

        if(intent.hasExtra(MainActivity.EXTRA_PLACE_DETAILS)){
            mFoodPlaceDetails = intent.getParcelableExtra(MainActivity.EXTRA_PLACE_DETAILS) as YouPickFoodPickerModel?
        }


        dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            cal.set(Calendar.YEAR, year)
            cal.set(Calendar.MONTH, month)
            cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            updateDateInView()
        }

        if(mFoodPlaceDetails != null){
            supportActionBar?.title = "Edit Food Place"
            binding?.etTitle?.setText(mFoodPlaceDetails!!.title)
            binding?.etDescription?.setText(mFoodPlaceDetails!!.description)
            binding?.etDate?.setText(mFoodPlaceDetails!!.date)
            binding?.etLocation?.setText(mFoodPlaceDetails!!.location)
            mLatitude = mFoodPlaceDetails!!.latitude
            mLongitude = mFoodPlaceDetails!!.longitude

            saveImageToInternalStorageDevice = Uri.parse(mFoodPlaceDetails!!.image)
            binding?.ivPlaceImage?.setImageURI(saveImageToInternalStorageDevice)
            binding?.btnSave?.text = "UPDATE"

        }

        updateDateInView()
        binding?.etTitle?.setOnClickListener(this)
        binding?.etDescription?.setOnClickListener(this)
        binding?.etDate?.setOnClickListener(this)
        binding?.etLocation?.setOnClickListener(this)
        binding?.tvAddImage?.setOnClickListener(this)
        binding?.btnSave?.setOnClickListener(this)
        binding?.tvSelectCurrentLocation?.setOnClickListener(this)

    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.etDate -> {
                DatePickerDialog(
                    this@ChooseFoodPlace, dateSetListener,
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH)
                ).show()
            }
            R.id.tv_add_image -> {
                val pictureDialog = AlertDialog.Builder(this@ChooseFoodPlace)
                pictureDialog.setTitle("Select Action")
                val pictureDialogItems = arrayOf("Select photo from Gallery", "Capture photo from camera")
                pictureDialog.setItems(pictureDialogItems) { _, which ->
                    when (which) {
                        0 -> choosePhotoFromGallery()

                        1 -> takePhotoFromCamera()
                    }
                }
                pictureDialog.show()

            }
            R.id.btn_save -> {
                when{
                    binding?.etTitle?.text.isNullOrEmpty() -> {
                        Toast.makeText(this,"Please enter title",Toast.LENGTH_LONG).show()
                    }
                    binding?.etDescription?.text.isNullOrEmpty() -> {
                        Toast.makeText(this,"Please enter description",Toast.LENGTH_LONG).show()
                    }
                    binding?.etLocation?.text.isNullOrEmpty() -> {
                        Toast.makeText(this,"Please enter location",Toast.LENGTH_LONG).show()
                    }
                    saveImageToInternalStorageDevice == null ->{
                        Toast.makeText(this,"Please select an image",Toast.LENGTH_LONG).show()
                    }else ->{
                        val foodPickerModel = YouPickFoodPickerModel(
                            if(mFoodPlaceDetails == null)
                                0 else mFoodPlaceDetails!!.id,
                            binding?.etTitle?.text.toString(),
                            saveImageToInternalStorageDevice.toString(),
                            binding?.etDescription?.text.toString(),
                            binding?.etDate?.text.toString(),
                            binding?.etLocation?.text.toString(),
                            mLatitude,
                            mLongitude
                        )
                        val dbHandler = DatabaseHandler(this)
                        if(mFoodPlaceDetails == null) {
                            val newAddFoodPlace = dbHandler.addFoodPlace(foodPickerModel)
                            if (newAddFoodPlace > 0) {
                                setResult(Activity.RESULT_OK)
                                finish()
                            }
                        }else{
                            val updateFoodPlace = dbHandler.updateFoodPlace(foodPickerModel)
                            if(updateFoodPlace > 0){
                                setResult(Activity.RESULT_OK)
                                finish()
                            }
                        }

                    }
                }
            }
            R.id.etLocation ->{
                try{
                    val apiKey = getString(R.string.google_maps_key)
                    if (!Places.isInitialized()) {
                        Places.initialize(applicationContext,apiKey)

                    }
                    val placesClient = Places.createClient(this)
                    val fields = listOf(Place.Field.ID, Place.Field.NAME)
                    val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
                        .build(this)
                    startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE)
                }catch(e: Exception){
                    e.printStackTrace()
                }
            }
            R.id.tv_select_current_location -> {
                if(!isLocationEnabled()){
                    Toast.makeText(this,"Sorry your location provider is turned off, please turn it on for this feature",Toast.LENGTH_LONG).show()
                    val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    startActivity(intent)
                }
                else if(!hasLocationPermissions()){
                    getLocationPermissions()
                }
                else{
                    requestNewLocationData()
                    Toast.makeText(this,"Location permissions granted!",Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    companion object {
        private const val CAMERA = 2
        private const val GALLERY = 1
        private const val LOCATION = 3
        private const val IMAGE_DIRECTORY = "YouPickFoodPicker"
        private const val PLACE_AUTOCOMPLETE_REQUEST_CODE = 4
    }

    private fun updateDateInView(){
        val myFormat = "MM.dd.yyyy"
        val sdf = SimpleDateFormat(myFormat, Locale.getDefault())
        binding?.etDate?.setText(sdf.format(cal.time).toString())
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    @SuppressLint("MissingPermission")
    private fun requestNewLocationData(){
        val mLocationRequest = LocationRequest()
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequest.interval = 1000
        mLocationRequest.numUpdates = 1

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        mFusedLocationProviderClient.requestLocationUpdates(mLocationRequest,mLocationCallBack,
            Looper.myLooper())
    }

    private val mLocationCallBack = object: LocationCallback(){
        override fun onLocationResult(locationResult: LocationResult?) {
            val mLastLocation: Location = locationResult!!.lastLocation

            mLatitude = mLastLocation.latitude
            Log.i("Current Latitude","$mLatitude")

            mLongitude = mLastLocation.longitude
            Log.i("Current Longitude","$mLongitude")

            val addressTask = GetAddressFromLatLng(this@ChooseFoodPlace, mLatitude, mLongitude)
            addressTask.setAddressListener(object: GetAddressFromLatLng.AddressListener {
              override fun onAddressFound(address: String?){
                  binding?.etLocation?.setText(address)
              }
              override fun onError(){
                  Log.e("Get Address::", "Something went wrong")
              }
            })
             addressTask.getAddress()
        }
    }

    private val openGalleryLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()){
            result ->
        if (result.resultCode == RESULT_OK && result.data != null){
            val imageBackGround = binding?.ivPlaceImage
            val galleryImageUri = result.data!!.data
            val selectedImageBitmap = MediaStore.Images.Media.getBitmap(this.contentResolver,galleryImageUri)

            saveImageToInternalStorageDevice = saveImageToInternalStorage(selectedImageBitmap)
            Log.e("Saved Image:", "Path :: $saveImageToInternalStorageDevice")
            imageBackGround!!.setImageBitmap(selectedImageBitmap)

        }
    }

    private val openCameraLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()){
            result ->
        if (result.resultCode == RESULT_OK) {
            val imageBackGround = binding?.ivPlaceImage
            val imageBitmap = result.data!!.extras!!.get("data") as Bitmap
            saveImageToInternalStorageDevice = saveImageToInternalStorage(imageBitmap)

            Log.e("Saved Image:", "Path :: $saveImageToInternalStorageDevice")
            imageBackGround?.setImageBitmap(imageBitmap)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            when (resultCode) {
                Activity.RESULT_OK -> {
                    data?.let {
                        val place = getPlaceFromIntent(data)
                        Log.i(TAG, "Place: ${place.name}, ${place.id}")
                    }
                }
                AutocompleteActivity.RESULT_ERROR -> {
                    // TODO: Handle the error.
                    data?.let {
                        val status = Autocomplete.getStatusFromIntent(data)
                        Log.i(TAG, status.statusMessage ?: "")
                    }
                }
                Activity.RESULT_CANCELED -> {
                    // The user canceled the operation.
                }
            }
            return
        }
        super.onActivityResult(requestCode, resultCode, data)
    }


    private fun takePhotoFromCamera(){
        if(hasCameraPermission() && hasWriteExternalStoragePermission() && hasReadExternalStoragePermission()){
            val takeCameraPhotoIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            openCameraLauncher.launch(takeCameraPhotoIntent)
        }
        if(!hasCameraPermission() || !hasReadExternalStoragePermission() || !hasWriteExternalStoragePermission()) {
            requestPermissions()
            timesRequestedPermission ++
        }
        if(timesRequestedPermission >= 3){
            showRationalDialogForPermissions()
        }
    }

    private fun choosePhotoFromGallery() {
            if(hasReadExternalStoragePermission() && hasWriteExternalStoragePermission()){
                val pickGalleryImageIntent = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                openGalleryLauncher.launch(pickGalleryImageIntent)
            }
            if(!hasReadExternalStoragePermission() || !hasWriteExternalStoragePermission()) {
                requestPermissions()
                timesRequestedPermission ++
            }
            if(timesRequestedPermission >= 3){
                showRationalDialogForPermissions()
            }
    }

    private fun getLocationPermissions() {
        if(!hasLocationPermissions()){
            requestPermissions()
            timesRequestedPermission ++
        }
        if(timesRequestedPermission >= 3){
            showRationalDialogForPermissions()
        }
    }

    private fun hasWriteExternalStoragePermission() =
        ActivityCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED

    private fun hasReadExternalStoragePermission() =
        ActivityCompat.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED

    private fun hasCameraPermission() =
        ActivityCompat.checkSelfPermission(this,Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED

    private fun hasLocationPermissions() =
        ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    && ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED

    private fun requestPermissions() {
        val permissionsToRequest = mutableListOf<String>()
        if (!hasWriteExternalStoragePermission()) {
            permissionsToRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        if (!hasReadExternalStoragePermission()) {
            permissionsToRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        if (!hasCameraPermission()) {
            permissionsToRequest.add(Manifest.permission.CAMERA)
        }
        if(!hasLocationPermissions()) {
            permissionsToRequest.add(Manifest.permission.ACCESS_COARSE_LOCATION)
                    && permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        if(permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsToRequest.toTypedArray(), 0)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ){
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == GALLERY || requestCode == CAMERA && requestCode == LOCATION && grantResults.isNotEmpty()){
            for(i in grantResults.indices){
                if(grantResults[i] == PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(
                        this@ChooseFoodPlace,
                        "${permissions[i]} Granted",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                if(grantResults[i] == PackageManager.PERMISSION_DENIED){
                    Toast.makeText(
                        this@ChooseFoodPlace,
                        "${permissions[i]} Denied",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
    private fun showRationalDialogForPermissions() {
        AlertDialog.Builder(this).setMessage(
            "It looks like you have turned off permission required for this feature.." +
                    " It can be enabled under the Application settings")
            .setPositiveButton("GO TO SETTINGS")
            { _, _->
                try {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package",packageName,null)
                    intent.data = uri
                    startActivity(intent)
                }catch (e: ActivityNotFoundException){
                    e.printStackTrace()
                }
            }.setNegativeButton("Cancel"){
                    dialog, _ ->
                dialog.dismiss()
            }.show()
    }

    // TODO save PNG format also
    private fun saveImageToInternalStorage(bitmap: Bitmap): Uri{
        val wrapper = ContextWrapper(applicationContext)
        var file = wrapper.getDir(IMAGE_DIRECTORY,Context.MODE_PRIVATE)
        file = File(file, "${UUID.randomUUID()}.jpg")

        try {
            val stream: OutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG,100,stream)
            stream.flush()
            stream.close()
        }catch (e: IOException){
            e.printStackTrace()
            Toast.makeText(this,"Sorry that file type is not supported..",Toast.LENGTH_LONG).show()
        }
        return Uri.parse(file.absolutePath)
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}
