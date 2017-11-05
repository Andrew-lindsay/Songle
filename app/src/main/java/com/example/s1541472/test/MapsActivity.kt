package com.example.s1541472.test

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.view.View
import android.widget.Toast
import android.widget.Toast.*
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationListener
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng

import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.data.kml.KmlLayer
import java.security.acl.LastOwnerException
import com.loopj.android.http.AsyncHttpClient
import com.loopj.android.http.FileAsyncHttpResponseHandler
import cz.msebera.android.httpclient.Header
import java.io.File
import java.io.FileInputStream



class MapsActivity : AppCompatActivity()
        ,OnMapReadyCallback
        ,GoogleApiClient.ConnectionCallbacks
        ,GoogleApiClient.OnConnectionFailedListener
        ,LocationListener
        {

    private lateinit var mMap: GoogleMap
    private lateinit var mGoogleApiClient: GoogleApiClient
    val PERMSISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1
    var mLocationPermissionGranted = false
    private var mLastLocation : Location? = null
    val TAG = "MapsActivity"
    lateinit var rfile: File


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        //create instance of google api client
        mGoogleApiClient = GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build()

    }

    override fun onStart() {
        super.onStart()
        mGoogleApiClient.connect()
    }

    override fun onStop(){
        super.onStop()
        if(mGoogleApiClient.isConnected){
           mGoogleApiClient.disconnect()
        }
    }

    //not required as of interface but setup function for mlocationRequest object
    fun createLocationRequest(){
        val mLocationRequest = LocationRequest()
        mLocationRequest.interval = 5000 //in miliseconds
        mLocationRequest.fastestInterval = 1000
        mLocationRequest.priority =LocationRequest.PRIORITY_HIGH_ACCURACY

        //permission check
        val permissionCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)

        if(permissionCheck == PackageManager.PERMISSION_GRANTED){
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient
                    ,mLocationRequest,this)
        }
    }

    override fun onConnected(connectionHint: Bundle?) {
        try{
            createLocationRequest()
        }catch(ise: IllegalStateException){
            println("IllegalStateException thrown [onConnected]")
        }

//        val permissionCheck2 = ContextCompat.checkSelfPermission(this,
//                Manifest.permission.ACCESS_FINE_LOCATION)

        if(ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            //no last location ready yet
            try {
                mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient)
            }catch(se:IllegalStateException){
                println("fuck")
                println(mGoogleApiClient.isConnected)
            }
        }else{
            ActivityCompat.requestPermissions(this,
                    arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION)
                        ,PERMSISSIONS_REQUEST_ACCESS_FINE_LOCATION)
        }

    }

    // changed when location updated main logic for being near markers goes here ?
    override fun onLocationChanged(current: Location?) {
        if(current == null){
            println("[onLocationChanged] unknown location")
        }else{
            //update location need for location button
            mLastLocation = current
            val currentloc = LatLng(current.latitude,current.longitude)
            mMap.animateCamera(CameraUpdateFactory.newLatLng(currentloc))
            println("Location changed")

            //set up tracking
        }
    }

    override fun onConnectionSuspended(flag: Int) {
        println(">>> connection Suspended")
    }


    override fun onConnectionFailed(p0: ConnectionResult) {
        println(">>> onConnection failed")
        //return to main screen
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
        mMap = googleMap

        val mapBounds = LatLngBounds(LatLng(55.942617,-3.192473)
                ,LatLng(55.946233,-3.184319))

        mMap.setMinZoomPreference(12.0F)
        mMap.setMaxZoomPreference(20.0F)
        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(mapBounds,0))

        try {
            mMap.isMyLocationEnabled = true
        }catch(se :SecurityException){
            println("Security exception thrown [onMapReady]")
        }

        mMap.uiSettings.isMyLocationButtonEnabled = true


        mMap.setOnMyLocationButtonClickListener {
            //my god this code is terrible
            if(mLastLocation != null){
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom( LatLng(mLastLocation!!.latitude, mLastLocation!!.longitude),20F))
            }
                true
        }
        //alternative way is to user the xml
        val client = AsyncHttpClient()
        client.get("https://www.inf.ed.ac.uk/teaching/courses/cslp/data/songs/01/map5.txt", object : FileAsyncHttpResponseHandler(/* Context */this) {
            override fun onSuccess(statusCode: Int, headers: Array<Header>, response: File) {

                println(response.toString())
                println("hi")
                var kmlInputstream  = FileInputStream(response)
                var layer = KmlLayer(mMap, kmlInputstream, applicationContext)
                layer.addLayerToMap()

            }

            override fun onFailure(statusCode: Int, headers: Array<out Header>?, throwable: Throwable?, file: File?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                println(">>> file failed to download")
            }
        })





//        if(ContextCompat.checkSelfPermission(this,
//                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
//            //no last location ready yet
//            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient)
//        }


    }



}

