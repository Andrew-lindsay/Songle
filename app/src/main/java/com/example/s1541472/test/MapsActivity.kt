package com.example.s1541472.test

import android.Manifest
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.view.GravityCompat
import android.support.v7.app.AlertDialog
import android.util.Xml
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
import com.google.android.gms.maps.model.*
import kotlinx.android.synthetic.main.activity_maps.*
import kotlinx.android.synthetic.main.guess_song_layout.view.*
import kotlinx.android.synthetic.main.toolbar_map.*
import kotlinx.android.synthetic.main.word_collected.view.*
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.*

/**
 * @author Andrew Lindsay
 * @version 0.000001 pre-alpha
 */
class MapsActivity : AppCompatActivity(),OnMapReadyCallback
        ,GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener,LocationListener {

    private lateinit var mMap: GoogleMap
    private lateinit var mGoogleApiClient: GoogleApiClient
    val PERMSISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1
    var mLocationPermissionGranted = false
    private var mLastLocation : Location? = null
    val TAG = "MapsActivity"
    private lateinit var songName: String
    private lateinit var songLink: String
    private lateinit var getSongCompleted: SharedPreferences
    private lateinit var markerFile:File
    private lateinit var lyricFile:File
    private var diff:Int = 0
    private var markersOnMap: ArrayList<MapMarker> = ArrayList()
    //does not need to be gobal in class
    private var maxDist:Float = 20F
    private var collectedwords:ArrayList<String> = ArrayList()
    private var lyrics:ArrayList<List<String>> = ArrayList()
    private var proceed = true
    private var gameStart:Long = 0L
    private var repeat:Boolean = false

    //TODO: get words collect in collectedWords array to display nicely /!
    //TODO: similarty checking upper and lower case -!
    //TODO: Fix hard coded link, inent message needed -!
    //TODO: timed challenge to award stars appropriately -!
    //TODO: can add count down timer on screen ?
    //TODO: help explain what each marker color is related to
    //TODO: on try of song stop from changing score -!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.toolbar_map)
        setSupportActionBar(toolbar)
        toolbar.setNavigationIcon(R.drawable.menu)

        //on button presses including those in the nav drawer
        toolbar.setNavigationOnClickListener { navBarOpen() }
        navCollect.setOnClickListener { wordCollect() }
        guess.setOnClickListener { guessButtonPress() }

        val intent = intent

        //get intent information
        diff = intent.getIntExtra("songle.difficultyTransfer",0)
        songName = intent.getStringExtra("songle.songName")
        songLink = intent.getStringExtra("songle.songLink")
        val mapFilePath = intent.getStringExtra("songle.mapFilePath")
        val lyricFilePath = intent.getStringExtra("songle.lyricsFilePath")
        repeat = intent.getBooleanExtra("songle.repeat",false)

        //file paths
        markerFile = File(mapFilePath)
        lyricFile = File(lyricFilePath)

        println(">>> Song to guess " + songName)

        //get song completion preference file to add to later if song completed
        val PREF_FILE = "CompletedSongs${diff}"

        println(">>>> Difficulty: " + diff)

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

        val lyricInput: FileReader = FileReader(lyricFile)
        var lyricsReader: BufferedReader = BufferedReader(lyricInput)
        var line = lyricsReader.readLine()

        //parsing the lyrics file so it can be indexed
        while( line != null){

            lyrics.add(line.split("\\s+".toRegex()))

            line = lyricsReader.readLine()
        }

        println(">>>>++++++<<<<")

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
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

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

            var locPoint = Location("marker")
            var dist:Float

            if(proceed == true) {
                var count = 0
                for (mark in markersOnMap) {

                    locPoint.latitude = mark.marker.position.latitude
                    locPoint.longitude = mark.marker.position.longitude

                    dist = current.distanceTo(locPoint)

                    //need to download the entire lyric file and parse into a array

                    if (dist < maxDist) {
                        collectedwords.add(mark.word)
                        collect(mark.word)
                        println(">>>> Here After button press")
                        mark.marker.remove()
                        markersOnMap.removeAt(count)
                        break
                    }

                    count++
                }
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

                if(mLastLocation1 != null) {
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                            LatLng(mLastLocation1.latitude, mLastLocation1.longitude), 20F))
                }
            }
            true
        }

        println(markerFile.toString())
        println(">>> KML files being placed on map")

        //KML file being parsed
        var kmlInputstream  = FileInputStream(markerFile)
        markersOnMap = parseXml(kmlInputstream)

        //start timer for game (star system)
        gameStart = System.currentTimeMillis()

    }

    override fun onBackPressed() {
        val exitBuilder = AlertDialog.Builder(this)

        //              var txtView = EditText(this)

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

    //green
    private fun guessButtonPress() {

        val exitBuilder = AlertDialog.Builder(this)

        //maybe put in different xml file then grab id
        var txtView = EditText(this)

        //sets padding for editText array so it looks better
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

            if(inputTxt.toLowerCase() == songName.toLowerCase()){

                //time in minutes to complete song

                val numOfStars = getNumOfStars()

                println("well done")
                onCorrectGuess(numOfStars)
                dialog2.dismiss()
            }else{
                txtView.setText("")
                dialog2.create()
            }
        }

        }

            var dialog = exitBuilder.setMessage("").setPositiveButton("Enter",{ _,_  ->

                val inputTxt = txtView.text.toString()

                //add star calculation here
                if(inputTxt.toLowerCase() == songName.toLowerCase()){

                    //need the number of stars awarded
                    val numOfStars = getNumOfStars()

                    println("well done")
                    onCorrectGuess(numOfStars)
                }else{
                    txtView.clearAnimation()
                    //not a great fix can be done better
                    (layout.parent as ViewGroup).removeView(layout)
                    dialog2.show()
            }
        }).create()

        dialog.show()

    }

    private fun getNumOfStars():Int{

        val timeToComp =  (System.currentTimeMillis() - gameStart).toDouble()/60000
        println(">>>>> Time in minutes taken to complete song: " + timeToComp )

        //need the number of stars awarded
        val numOfStars:Int =  if(repeat){
            getSongCompleted.getInt(songName,0)
        }else {
            when {
                timeToComp < 10 -> 5
                timeToComp < 20 -> 4
                timeToComp < 30 -> 3
                timeToComp < 40 -> 2
                else -> 1
            }
        }

        return numOfStars
    }

    //once song has been correctly guess run
    private fun onCorrectGuess(numOfStars:Int) {
        // can add include for layout guess_song_layout
        var congratBuilder = AlertDialog.Builder(this)

        val inflater = layoutInflater

        val a = inflater.inflate(R.layout.guess_song_layout,null)

        a.txtsong.text = songName

        var starArray = arrayOf(a.star1,a.star2,a.star3,a.star4,a.star5)



        for(index in 0..(numOfStars - 1) ){
            starArray[index].setImageResource(android.R.drawable.btn_star_big_on)
        }

        if(repeat){
            a.txtmain.text = "You have already completed this song, stars can now only be award if played by selecting random"
        }

        congratBuilder
                .setView(a)
                .setNegativeButton("return" ,{ _ , _ ->
                    onSongCompleted(numOfStars)
                    super.onBackPressed()
        }).setPositiveButton("play song",{ _, _ ->
            onSongCompleted(numOfStars)
            watchVideoLink(songLink)
            super.onBackPressed()
        }).setOnDismissListener({
            onSongCompleted(numOfStars)
            super.onBackPressed()
        })

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

        var wordscold = collectedwords.toString()

        exitBuilder
                .setTitle("Words Colected:")
                .setPositiveButton("Ok",{_,_ ->

                }).setMessage( wordscold.substring(1,wordscold.length-1) ).create().show()
    }

    private fun collect(word:String){
        val wordBuilder = AlertDialog.Builder(this)

        proceed = false
        //building completed song List
        val inflater = layoutInflater
        val wordCollectView= inflater.inflate(R.layout.word_collected,null)

        wordCollectView.wordCollect.text = word

        wordBuilder
                .setTitle("Word Collected:")
                .setView(wordCollectView)
                .setPositiveButton("Ok",{_,_ ->
                    proceed = true
                }).setOnDismissListener({
                proceed = true
        }).create().show()

        println(">>>> Users has pressed ok after seeing word")

    }

    private fun onSongCompleted(numOfStars: Int){
        println(">>> $diff")
        var setSongComplete = getSongCompleted.edit()
        setSongComplete.putInt(songName,numOfStars)
        setSongComplete.apply()
    }

    //--------------------------------------------------------------------------------------
    //---------------------------XML parser starts here-------------------------------------


    @Throws(XmlPullParserException::class, IOException::class)
    fun parseXml(strm:FileInputStream):ArrayList<MapMarker>{

        //use kotlin block to save on strm.close()
        strm.use {
            val parser = Xml.newPullParser()
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES,false)
            parser.setInput(strm,null)
            parser.nextTag()
            parser.nextTag()
            return readMarkers(parser)
        }
    }


    @Throws(XmlPullParserException::class, IOException::class)
    private fun readMarkers(parser: XmlPullParser):ArrayList<MapMarker>{
        val songsParsed = ArrayList<MapMarker>()

        parser.require(XmlPullParser.START_TAG,null,"Document")
        while(parser.next() != XmlPullParser.END_TAG){
            if(parser.eventType != XmlPullParser.START_TAG ){
                //continues to next iteration of the loop
                continue
            }

            if(parser.name == "Placemark"){
                songsParsed.add(readMarker(parser))
            }else {
                skip(parser)
            }
        }

        return songsParsed
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun readMarker(parser: XmlPullParser):MapMarker{
        parser.require(XmlPullParser.START_TAG,null,"Placemark")
        var word = ""
        var desc = ""

        var points:Array<Double> = arrayOf(0.0,0.0)

        while(parser.next() != XmlPullParser.END_TAG){
            if(parser.eventType != XmlPullParser.START_TAG){
                continue
            }
            when(parser.name){
                "name" -> word = readName(parser)
                "description" -> desc = readDesc(parser)
                "Point" -> points = readPos(parser)
                else -> skip(parser)
            }
        }

        //add markers parsed to map

        //if descriptor is a certain type set icon
        val icon = when(desc) {
            "unclassified" -> BitmapDescriptorFactory.fromBitmap(bitmapScale(R.drawable.wht_blank))
            "boring" -> BitmapDescriptorFactory.fromBitmap(bitmapScale(R.drawable.ylw_blank))
            "notboring" -> BitmapDescriptorFactory.fromBitmap(bitmapScale(R.drawable.ylw_circle))
            "interesting" -> BitmapDescriptorFactory.fromBitmap(bitmapScale(R.drawable.orange_diamond))
            "veryinteresting" -> BitmapDescriptorFactory.fromBitmap(bitmapScale(R.drawable.red_stars))
            else -> BitmapDescriptorFactory.fromBitmap(bitmapScale(R.drawable.design_password_eye))
        }

        //get word, with -1 to offset for array indexing
        val wordNum = word.substringAfter(':').toInt() -1
        val lineNum = word.substringBefore(':').toInt() - 1

        //gets word that marker is associated with from list of list created from lyrics.txt
        val wordFromFile = lyrics[lineNum][wordNum]

        return MapMarker(mMap.addMarker(MarkerOptions()
                .position( LatLng(points[0],points[1]))
                .title(desc)
                .icon(icon)),wordFromFile)
    }

    fun bitmapScale(resrc:Int):Bitmap {

        val bitmapIn = BitmapFactory.decodeResource(resources,resrc)

        val scall_zero_to_one_f :Float = 0.5F

        val bitmapOut :Bitmap = Bitmap.createScaledBitmap(bitmapIn,
            Math.round(bitmapIn.width * scall_zero_to_one_f),
            Math.round(bitmapIn.height * scall_zero_to_one_f), false)

        return bitmapOut
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun readPos(parser: XmlPullParser): Array<Double>{

        var pos:Array<Double> = arrayOf(1.0,1.0)

        var posStr = ""

        parser.require(XmlPullParser.START_TAG,null,"Point")

        while(parser.next() != XmlPullParser.END_TAG){
            if(parser.eventType != XmlPullParser.START_TAG){
                continue
            }
            when(parser.name){
                "coordinates" -> posStr = readCoord(parser)
                else -> skip(parser)
            }
        }

        posStr = posStr.subSequence(0,posStr.length-2).toString()

        pos[0] = posStr.substringAfter(',').toDouble()
        pos[1] = posStr.substringBefore(',').toDouble()

        return pos
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun readCoord(parser: XmlPullParser): String {

        parser.require(XmlPullParser.START_TAG,null,"coordinates")
        val posStr = readText(parser)
        parser.require(XmlPullParser.END_TAG,null,"coordinates")
        return posStr
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun readTitle(parser: XmlPullParser): String {
        parser.require(XmlPullParser.START_TAG,null,"Title")
        val title = readText(parser)
        parser.require(XmlPullParser.END_TAG,null,"Title")
        return title
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun readDesc(parser: XmlPullParser): String {
        parser.require(XmlPullParser.START_TAG,null,"description")
        val artist = readText(parser)
        parser.require(XmlPullParser.END_TAG,null,"description")
        return artist
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun readName(parser: XmlPullParser): String {
        parser.require(XmlPullParser.START_TAG,null,"name")
        val name = readText(parser)
        parser.require(XmlPullParser.END_TAG,null,"name")
        return name
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun skip(parser: XmlPullParser) {
        if(parser.eventType != XmlPullParser.START_TAG){
            throw IllegalStateException()
        }
        var depth = 1
        while(depth != 0){
            when(parser.next()){
                XmlPullParser.END_TAG -> depth--
                XmlPullParser.START_TAG -> depth++
            }
        }

    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun readText(parser: XmlPullParser): String {
        var result = ""
        if(parser.next() == XmlPullParser.TEXT){
            result = parser.text
            parser.nextTag()
        }
        return result
    }


}


