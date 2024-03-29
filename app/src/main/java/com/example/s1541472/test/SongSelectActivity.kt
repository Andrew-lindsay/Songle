package com.example.s1541472.test

import android.app.AlertDialog
import android.content.*
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.activity_song_select.*
import android.widget.AdapterView.OnItemClickListener
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
import java.util.Collections.shuffle
import kotlin.collections.ArrayList

/**
 * @author Andrew Lindsay
 * @version 0.000001 pre-alpha
 */
class SongSelectActivity : AppCompatActivity() {

    var songs = ArrayList<Song>()
    //difficulty default is 0(easy)
    var diff: Int = 0
    private val diffSend:String = "songle.difficultyTransfer"
    private lateinit var getSongCompleted: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_song_select)


        val intent = intent

        diff = intent.getIntExtra("songle.difficultySet",0)

        val PREF_FILE = "CompletedSongs${diff}"

        getSongCompleted = getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE)

        //debug
        println(PREF_FILE)

        //diff default is easy
        println("Difficulty Set: " + diff)

        //change Title displayed in action bar
        title = when(diff){
            0 -> "Easy"
            1 -> "Medium"
            2 -> "Hard"
            3 -> "Extra Hard"
            4 -> "Extreme"
            else -> "ERROR" }

        //aync download task
        downloadSongs()

        Random.setOnClickListener {
            if(songs.size != 0) {
                val index = ((Math.random() * 100).toInt() % songs.size)
                switchtoLoading(songs[index].title,songs[index].number,songs[index].link)
            }
        }

        val infoBuilder = AlertDialog.Builder(this)

        //listView displayed on screen on click action below excuted
        songList.onItemClickListener = object: OnItemClickListener {

            override fun onItemClick(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {

                val entrySong = songs[p2]

                //if song completed display info when clicked
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
                        switchtoLoading(entrySong.title,entrySong.number,entrySong.link,repeat = true)

                    })
                    infoBuilder.setNeutralButton("Listen",DialogInterface.OnClickListener { dialog, id ->
                        watchVideoLink(entrySong.link)
                    })
                    infoBuilder.create().show()

                    //when just a question mark go straight to playing so dont display info
                }else switchtoLoading(entrySong.title,entrySong.number,entrySong.link)

            }
        }

    }

    override fun onResume() {
        super.onResume()
    }

    override fun onRestart() {
        //refresh song list so when user returns from map after completing a song
        //the list displayed the name of the song instead of a question mark
        refreshSongList()
        super.onRestart()

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

        //custom list adapter written to get stars to display properly
        //takes array list of songs

        //filter all songs that are not complete before creating an adapter
        val compAdapter = songListAdapter(ArrayList(songs.filter { it.complete > 0 }),this)

        //check song in list
        songs.map { println(it) }

        compList.adapter = compAdapter

        val diabuild = AlertDialog.Builder(this)

        //building completed song List
        val inflater = layoutInflater
        val ve = inflater.inflate(R.layout.comlete_song_title,null)

        diabuild.setView(compList)
                .setCustomTitle(ve)

        val songComp = diabuild.create()

        //do action on icon on app bar being pressed
        return when (item.itemId) {
            R.id.comp_song ->  {
                songComp.show()
                true}
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun refreshSongList(){
        println(">>> diff: $diff")

        //print all songs in list a long with there number of stars
        songs.map{ println("Title: " + it.title +" complete: " + it.complete) }

        //update all song completed fields in the song objects as
        songs.map{ it.complete = getSongCompleted.getInt(it.title,0)}

        //check songs have been updated correctly
        songs.map{ println("Title: " + it.title +" complete: " + it.complete) }

        //update adapter so list display properly
        var adapter = ArrayAdapter(this@SongSelectActivity,R.layout.simplerow,songs)
        songList.adapter = adapter
    }

    //change activity when song is selected
    private fun switchtoLoading(songTitle:String,songNumber:Int,songLink:String,repeat:Boolean = false){
        val intent = Intent(this,LoadingScreen::class.java)
        intent.putExtra(diffSend,diff)
        intent.putExtra("songle.songName",songTitle)
        intent.putExtra("songle.songNumber",songNumber)
        intent.putExtra("songle.songLink",songLink)
        intent.putExtra("songle.repeat",repeat)

        startActivity(intent)
    }

    //switch to youtube to play song link
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

        //create snack bar so user can select to retry donwload
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

                //shuffle so song are not in same place for each difficulty
                //seeded with difficulties number so order is reproducible
                shuffle(songs,java.util.Random(diff.toLong()))

                var adapter = ArrayAdapter(this@SongSelectActivity,R.layout.simplerow,songs)

                songList.adapter = adapter

                }

            override fun onFailure(statusCode: Int, headers: Array<out Header>?, throwable: Throwable?, file: File?) {
                //display snack bar and wait
                songDownloadFail()
                println(">>> file failed to download")
            }

            override fun onUserException(error: Throwable?) {
                println(">>>> user error occurred")
                songDownloadFail()
            }

            override fun onRetry(retryNo: Int) {
                super.onRetry(retryNo)
            }
        })
    }

    //XML parser starts here

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
        var completed: Int

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

        //accessing preference files
        completed = getSongCompleted.getInt(title,0)
        println(">>> XML parser create song completed: $completed Title: $title")

        //create song object with parsed information and completed song value
        return Song(number,artist,title,link,completed)
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
