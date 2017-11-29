package com.example.s1541472.test

import android.Manifest
import android.content.*
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.view.GravityCompat
import android.support.v7.app.AlertDialog
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
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
import com.google.maps.android.data.kml.KmlLayer
import kotlinx.android.synthetic.main.activity_maps.*
import kotlinx.android.synthetic.main.guess_song_layout.view.*
import kotlinx.android.synthetic.main.toolbar_map.*
import java.io.File
import java.io.FileInputStream

/**
 * @author Andrew Lindsay
 * @version 0.000001 pre-alpha
 */
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
    private lateinit var songName: String
    private val link: String = "https://youtu.be/fJ9rUzIMcZQ"
    private lateinit var getSongCompleted: SharedPreferences
    private lateinit var markerFile:File
    private lateinit var lyricFile:File
    private var diff:Int = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.toolbar_map)
        setSupportActionBar(toolbar)
        toolbar.setNavigationIcon(R.drawable.menu)

//        val mDrawerToggle = ActionBarDrawerToggle(this, drawer_layout, 0, 0)

        toolbar.setNavigationOnClickListener { navBarOpen() }

        navCollect.setOnClickListener { wordCollect() }

        val intent = intent



        //get intent information
        diff = intent.getIntExtra("songle.difficultyTransfer",0)
        songName = intent.getStringExtra("songle.songName")
        val mapFilePath = intent.getStringExtra("songle.mapFilePath")
        val lyricFilePath = intent.getStringExtra("songle.lyricsFilePath")

        //file paths
        markerFile = File(mapFilePath)
        lyricFile = File(lyricFilePath)

        println(">>> Song to guess " + songName)

        //get song completion preference file to add to later if song completed
        val PREF_FILE = "CompletedSongs${diff}"

        println("Difficulty: " + diff)

        getSongCompleted = getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE)

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

        guess.setOnClickListener { guessButtonPress() }

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
                println("broken")
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
//            mLastLocation = current
//
//            mMap.addCircle(CircleOptions()
//                    .center(LatLng(current.latitude, current.longitude))
//                    .radius(12.0)
//                    .strokeColor(Color.RED))


            val currentloc = LatLng(current.latitude,current.longitude)

            if(locFollow.isChecked) {
                mMap.animateCamera(CameraUpdateFactory.newLatLng(currentloc))
            }

            println(">>>Location changed")
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

        mMap.setPadding(10,210,10,10)

        mMap.setMinZoomPreference(12.0F)
        mMap.setMaxZoomPreference(20.0F)
        mMap.setOnMapLoadedCallback{ mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(mapBounds, 0))}

        try {
            mMap.isMyLocationEnabled = true
        }catch(se :SecurityException){
            println("Security exception thrown [onMapReady]")
        }

        mMap.uiSettings.isMyLocationButtonEnabled = true


        mMap.setOnMyLocationButtonClickListener {
            //my god this code is terrible can be fixed by calling google api and not assigning mLastLocation in on location change

            if(ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                //no last location ready yet
                val mLastLocation1 = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient)

                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                        LatLng(mLastLocation1.latitude, mLastLocation1.longitude), 20F))
            }
            true
        }

        //set KML file now
        println(markerFile.toString())
        println(">>> KML files being placed on map")
        var kmlInputstream  = FileInputStream(markerFile)
        var layer = KmlLayer(mMap, kmlInputstream, applicationContext)
        layer.addLayerToMap()

    }

            override fun onBackPressed() {
                val exitBuilder = AlertDialog.Builder(this)
//                var txtView = EditText(this)

                exitBuilder
                        .setTitle("Warning")
                        .setMessage("Are you sure you want to exit all progress will be lost")
                        .setPositiveButton("Yes", DialogInterface.OnClickListener{ _, _ ->
                            super.onBackPressed()
                        })
                        .setNegativeButton("No",{_,_ -> })
                        .create()
                        .show()
            }

            private fun guessButtonPress() {

                val exitBuilder = AlertDialog.Builder(this)

                //maybe put in different xml file then grab id
                var txtView = EditText(this)

                var layout = LinearLayout(this)
                layout.orientation = LinearLayout.VERTICAL
                var params = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                            params.setMargins(70, 0, 70, 0)

                layout.addView(txtView,params)

                exitBuilder
                        .setTitle("Enter Your Guess:")
                        .setView(layout)
                        .setPositiveButton("Enter",null)
                        .setNegativeButton("Exit",{_,_ ->

                        })

                var dialog2 = exitBuilder.setMessage("Sorry that is not correct").create()

                dialog2.setOnShowListener { dialog2.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                        val inputTxt = txtView.text.toString()
                        if(inputTxt == songName){
                            println("well done")
                            onCorrectGuess()
                            dialog2.dismiss()
                        }else{
                            txtView.setText("")
                            dialog2.create()
                        }
                    }
                }

                var dialog = exitBuilder.setMessage("").setPositiveButton("Enter",{ _,_  ->
                    val inputTxt = txtView.text.toString()
                    if(inputTxt == songName){
                        println("well done")
                        onCorrectGuess()
                    }else{
                        txtView.clearAnimation()
                        //not a great fix can be done better
                        (layout.parent as ViewGroup).removeView(layout)
                        dialog2.show()
                    }
                }).create()

                dialog.show()

            }

            private fun onCorrectGuess() {
                // can add include for layout guess_song_layout
                var congratBuilder = AlertDialog.Builder(this)

                val inflater = layoutInflater

                val a = inflater.inflate(R.layout.guess_song_layout,null)

                a.txtsong.text = songName

                congratBuilder
                        .setView(a)
                        .setNegativeButton("return" ,{ _ , _ ->

                            onSongCompleted()
                            super.onBackPressed()

                }).setOnDismissListener {
                    onSongCompleted()
                    super.onBackPressed()
                }

                val congratDialog = congratBuilder.create()

                congratDialog.show()
            }

            private fun watchVideoLink(id: String) {

                val id_fix = id.substring(17,id.length)

                val applicationIntent = Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + id_fix))
                val browserIntent = Intent(Intent.ACTION_VIEW,
                        Uri.parse("http://www.youtube.com/watch?v=" + id_fix))
                try {
                    startActivity(applicationIntent)
                } catch (ex: ActivityNotFoundException) {
                    startActivity(browserIntent)
                }

            }

            private fun switchtoSongSelect(){
                val intent1 = Intent(this,SongSelectActivity::class.java)
                startActivity(intent1)
            }

            private fun navBarOpen(){

                if(drawer_layout.isDrawerOpen(GravityCompat.START)){
                    drawer_layout.closeDrawer(GravityCompat.START)
                }else{
                    drawer_layout.openDrawer(GravityCompat.START)
                }
            }

            private fun wordCollect(){
                val exitBuilder  = AlertDialog.Builder(this)

                exitBuilder
                        .setTitle("Words Colected:")
                        .setPositiveButton("Ok",{_,_ ->

                        }).setMessage("the, Forrest, will, rise, appropriation").create().show()
            }

            private fun collect(){
                val wordBuilder = AlertDialog.Builder(this)
                wordBuilder
                        .setTitle("Word Collected:")
                        .setView(R.layout.word_collected)
                        .setPositiveButton("Ok",{_,_ ->

                        }).create().show()

            }

            private fun onSongCompleted(){
                println(">>> $diff")
                var setSongComplete = getSongCompleted.edit()
                setSongComplete.putInt(songName,1)
                setSongComplete.apply()
            }
        }


