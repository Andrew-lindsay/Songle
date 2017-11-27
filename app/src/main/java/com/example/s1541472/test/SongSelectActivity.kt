package com.example.s1541472.test

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.activity_song_select.*
import android.widget.AdapterView.OnItemClickListener
import android.content.ActivityNotFoundException
import android.net.Uri
import android.support.design.widget.Snackbar

import android.util.Xml

import android.view.Menu
import android.view.MenuItem
import android.widget.*
import com.loopj.android.http.AsyncHttpClient
import com.loopj.android.http.FileAsyncHttpResponseHandler
import cz.msebera.android.httpclient.Header
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.File
import java.io.FileInputStream
import java.io.IOException

class SongSelectActivity : AppCompatActivity() {

    var songs = ArrayList<Song>()

    //difficulty default is 0(easy)
    var diff: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_song_select)

        val intent = intent

        diff = intent.getIntExtra("songle.difficultySet",0)

        //diff default is easy
        println("Difficulty Set: " + diff)

        //change Title displayed in action bar
        title = when(diff){
            0 -> "Easy"
            1 -> "Medium" 2 -> "Hard"
            3 -> "Extra Hard"
            4 -> "Extreme"
            else -> "ERROR" }

        //aync download task
        downloadSongs()

        Random.setOnClickListener {  }



        //testing code
//        for(i in 1..12){
//            val song1 = Song(title = "${i}", number = 1, artist = "me", link = "None", complete = 0)
//            songs.add(song1)
//        }
//
//        val song1 = Song(title = "Bohemian Rhapsody", number = 1, artist = "Queen", link = "https://youtu.be/fJ9rUzIMcZQ", complete = 1)
//        val song2 = Song(title = "I Fought the Law", number = 1, artist = "The Clash", link = "", complete = 1)
//        songs.add(5,song1)
//        songs.add(7,song2)
        //end of testing code


        //run after download and parsing
//        val adapter = ArrayAdapter(this,R.layout.simplerow,songs)
//
//        songList.adapter = adapter


        val infoBuilder = AlertDialog.Builder(this)

        songList.onItemClickListener = object: OnItemClickListener {
            override fun onItemClick(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                val entrySong = songs[p2]

                if (entrySong.complete  > 0){

                    infoBuilder.setMessage("""Artist: ${entrySong.artist}
                        |Title: ${entrySong.title}
                        |Link : ${entrySong.link}
                    """.trimMargin())
                            .setTitle("Song Info")
                    //set ok button for exit
                    infoBuilder.setPositiveButton(R.string.ok, DialogInterface.OnClickListener { dialog, id ->
                        // User clicked return button
                    })
                    infoBuilder.setNegativeButton("Play again", DialogInterface.OnClickListener { dialog, id ->
                        // User clicked play again button
                        switchtoMap()
                    })
                    infoBuilder.setNeutralButton("Listen",DialogInterface.OnClickListener { dialog, id ->
                        watchVideoLink(entrySong.link)
                    })
                    infoBuilder.create().show()

                }else switchtoMap()

            }
        }

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_song_select, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        //define new list display object to fill dialog box
        val compList = ListView(this)

//        val complist = ArrayList<Song>()

//        val s = Song(title = "Bohemian Rhapsody", number = 1, artist = "Queen", link = "https://youtu.be/fJ9rUzIMcZQ", complete = 1)
//        val r = Song(title = "I Fought the Law", number = 1, artist = "Queen", link = "https://youtu.be/fJ9rUzIMcZQ", complete = 2)
//        complist.add(s)
//        complist.add(r)

        //custom list adapter written to get stars to display properly
        //takes array list of songs
        val compAdapter = songListAdapter(songs,this)

        songs.map { println(it) }


        compList.adapter = compAdapter



        val diabuild = AlertDialog.Builder(this)

        //building completed song List
        val inflater = layoutInflater
        val ve = inflater.inflate(R.layout.word_collected,null)

        diabuild.setView(compList)
                .setCustomTitle(ve)

        val songComp = diabuild.create()

        return when (item.itemId) {
            R.id.comp_song ->  {
                songComp.show()
                true}
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun switchtoMap(){
        val intent = Intent(this,MapsActivity::class.java)
        startActivity(intent)

    }

    fun watchVideoLink(id: String) {

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

    private fun songDownloadFail(){

        Snackbar.make(topLayer , "Song list download has failed", Snackbar.LENGTH_INDEFINITE)
                .setAction("Retry", View.OnClickListener {
                    downloadSongs()
                }).show()

    }

    private fun downloadSongs(){

        val client = AsyncHttpClient()
        client.get("https://www.inf.ed.ac.uk/teaching/courses/cslp/data/songs/songs.xml", object : FileAsyncHttpResponseHandler(this) {

            override fun onSuccess(statusCode: Int, headers: Array<Header>, response: File) {


                //have to parse here or download in a previous download activity
                println(response.toString())

                var songStrm: FileInputStream = FileInputStream(response)

                songs = parseXml(songStrm)

                var adapter = ArrayAdapter(this@SongSelectActivity,R.layout.simplerow,songs)

                songList.adapter = adapter

                }

            override fun onFailure(statusCode: Int, headers: Array<out Header>?, throwable: Throwable?, file: File?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                println(">>> file failed to download")
            }

            override fun onUserException(error: Throwable?) {

                println("you fucked up, man")
                songDownloadFail()
            }

            override fun onRetry(retryNo: Int) {
                super.onRetry(retryNo)
            }
        })
    }

    @Throws(XmlPullParserException::class,IOException::class)
    fun parseXml(strm:FileInputStream):ArrayList<Song>{

        //use kotlin block to save on strm.close()
        strm.use {
            val parser = Xml.newPullParser()
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES,false)
            parser.setInput(strm,null)
            parser.nextTag()
            return readSongs(parser)
        }
    }


    @Throws(XmlPullParserException::class,IOException::class)
    private fun readSongs(parser: XmlPullParser):ArrayList<Song>{
        val songsParsed = ArrayList<Song>()

        parser.require(XmlPullParser.START_TAG,null,"Songs")
        while(parser.next() != XmlPullParser.END_TAG){
            if(parser.eventType != XmlPullParser.START_TAG ){
                //continues to next iteration of the loop
                continue
            }

            if(parser.name == "Song"){
                songsParsed.add(readSong(parser))
            }else {
                skip(parser)
            }
        }

        return songsParsed
    }

    @Throws(XmlPullParserException::class,IOException::class)
    private fun readSong(parser: XmlPullParser):Song{
        parser.require(XmlPullParser.START_TAG,null,"Song")
        var number = 0
        var artist = ""
        var title = ""
        var link = ""

        while(parser.next() != XmlPullParser.END_TAG){
            if(parser.eventType != XmlPullParser.START_TAG){
                continue
            }
            when(parser.name){
                "Number" -> number = readNumber(parser)
                "Artist" -> artist = readArtist(parser)
                "Title" -> title = readTitle(parser)
                "Link" -> link = readLink(parser)
                else -> skip(parser)
            }
        }
        return Song(number,artist,title,link)
    }


    @Throws(XmlPullParserException::class,IOException::class)
    private fun readLink(parser: XmlPullParser): String {
        parser.require(XmlPullParser.START_TAG,null,"Link")
        val link = readText(parser)
        parser.require(XmlPullParser.END_TAG,null,"Link")
        return link
    }

    @Throws(XmlPullParserException::class,IOException::class)
    private fun readTitle(parser: XmlPullParser): String {
        parser.require(XmlPullParser.START_TAG,null,"Title")
        val title = readText(parser)
        parser.require(XmlPullParser.END_TAG,null,"Title")
        return title
    }

    @Throws(XmlPullParserException::class,IOException::class)
    private fun readArtist(parser: XmlPullParser): String {
        parser.require(XmlPullParser.START_TAG,null,"Artist")
        val artist = readText(parser)
        parser.require(XmlPullParser.END_TAG,null,"Artist")
        return artist
    }

    @Throws(XmlPullParserException::class,IOException::class)
    private fun readNumber(parser: XmlPullParser): Int {
        parser.require(XmlPullParser.START_TAG,null,"Number")
        val number = readText(parser)
        parser.require(XmlPullParser.END_TAG,null,"Number")
        return Integer.parseInt(number)
    }

    @Throws(XmlPullParserException::class,IOException::class)
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

    @Throws(XmlPullParserException::class,IOException::class)
    private fun readText(parser: XmlPullParser): String {
        var result = ""
        if(parser.next() == XmlPullParser.TEXT){
            result = parser.text
            parser.nextTag()
        }
        return result
    }
}
