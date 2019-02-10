package com.example.delta.mapapp

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationProvider
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.speech.RecognizerIntent
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.KeyEvent
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.google.android.gms.location.FusedLocationProviderClient

import com.google.android.gms.location.LocationServices

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import java.io.IOException
import java.util.*


class MapsActivity() : AppCompatActivity(), OnMapReadyCallback {

    //vars
    private lateinit var mMap: GoogleMap
    private var mapsLocationPermissionsGranted: Boolean = false
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private val permission = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)

    companion object {
        const val TAG = "MapsActivity"
        const val FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION
        const val LOCATION_PERMISSION_REQUEST_CODE = 1234
        const val DEFAULT_ZOOM: Float = 15f;
        const val REQUEST_CODE_SPEECH_INPUT = 100

    }

    //widgets
    private lateinit var searchLocationText: EditText
    private lateinit var speechToText: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        searchLocationText = findViewById<EditText>(R.id.input_search)
        speechToText = findViewById<ImageView>(R.id.voice_search)

        getLocationPermission()


    }

    fun initMap() {
        Log.d(TAG, "initMap: initializing map")
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    private fun init() {
        Log.d(TAG, "init:initializing")
        searchLocationText.setOnEditorActionListener(object : TextView.OnEditorActionListener {
            override fun onEditorAction(textView: TextView, actionId: Int, keyEvent: KeyEvent): Boolean {
                if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE || keyEvent.getAction() == KeyEvent.ACTION_DOWN || keyEvent.getAction() == KeyEvent.KEYCODE_ENTER) {
                    geoLocate()
                }
                return false
            }
        })

        speechToText.setOnClickListener{
            listening();
        }


        hideSoftKeyboard()
    }

    private fun listening() {
        val mIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        mIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        mIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        mIntent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Hi... Say something!")

        try{
            startActivityForResult(mIntent, REQUEST_CODE_SPEECH_INPUT)
        }catch(e: Exception){
            Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
        }


    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode){
            REQUEST_CODE_SPEECH_INPUT->{
                if(resultCode == Activity.RESULT_OK && null != data){
                val result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                searchLocationText.setText(result[0])
                geoLocate()

                }
            }
        }
    }

    private fun geoLocate() {
        Log.d(TAG, "geoLocate:geolocating")

        var searchString: String = searchLocationText.text.toString()
        var geocoder: Geocoder = Geocoder(this)
        var list = listOf<Address>()
        try {
            list = geocoder.getFromLocationName(searchString, 1)

        } catch (e: IOException) {
            Log.e(TAG, "geoLocate: IOException: " + e.message)
        }

        if (list.size > 0) {
            var address = list.get(0)
            Log.d(TAG, "geoLocate: found a location: " + address.toString())

            moveCamera(LatLng(address.latitude, address.longitude), DEFAULT_ZOOM, address.getAddressLine(0))
        }
    }

    private fun getDeviceLocation() {
        Log.d(TAG, "getDeviceLocation: getting device location")
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        try {
            if (mapsLocationPermissionsGranted) {
                val location = fusedLocationProviderClient.lastLocation
                location.addOnCompleteListener(object : OnCompleteListener<Location> {
                    override fun onComplete(task: Task<Location>) {
                        if (task.isSuccessful) {
                            Log.d(TAG, "onComplete: found Location")
                            var currentLocation = task.result as Location
                            moveCamera(LatLng(currentLocation.latitude, currentLocation.longitude), DEFAULT_ZOOM, "My Location")
                        } else {
                            Log.d(TAG, "onComplete: something else")
                        }
                    }

                })
            }

        } catch (e: SecurityException) {
            Log.e(TAG, "getDeviceLocation: SecurityException:" + e.message)
        }
    }

    private fun getLocationPermission() {
        Log.d(TAG, "getLocationPermission: getting location permission")


        if (ContextCompat.checkSelfPermission(this.applicationContext, FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mapsLocationPermissionsGranted = true
            initMap()
        } else {
            ActivityCompat.requestPermissions(this, permission, LOCATION_PERMISSION_REQUEST_CODE)
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */

    override fun onMapReady(googleMap: GoogleMap) {
        Toast.makeText(this, "Map is ready", Toast.LENGTH_SHORT).show()
        Log.d(TAG, "onMapReady: map is ready")
        mMap = googleMap

        if (mapsLocationPermissionsGranted) {
            getDeviceLocation();

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return
            }
            mMap.isMyLocationEnabled = true
            //mMap.uiSettings.isMyLocationButtonEnabled = false

            init()

        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        mapsLocationPermissionsGranted = false

        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE ->
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mapsLocationPermissionsGranted = true
                    initMap()
                }
        }
    }

    fun moveCamera(latlng: LatLng, zoom: Float, title: String) {
        Log.d(TAG, "moveCamera: moving the cmaera to : lat:" + latlng.latitude + ", lng: " + latlng.longitude)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng, zoom))

        if (!title.equals("My Location")) {
            var options = MarkerOptions()
                    .position(latlng)
                    .title(title)
            mMap.addMarker(options)
        }

        hideSoftKeyboard()


    }

    private fun hideSoftKeyboard() {
        this.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
    }
}

