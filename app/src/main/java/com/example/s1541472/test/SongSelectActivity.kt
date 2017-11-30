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

    val diffSend:String = "songle.difficultyTransfer"

    lateinit var getSongCompleted: SharedPreferences

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
                switchtoLoading(songs[index].title,songs[index].number)
            }
        }

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
                        switchtoLoading(entrySong.title,entrySong.number)

                    })
                    infoBuilder.setNeutralButton("Listen",DialogInterface.OnClickListener { dialog, id ->
                        watchVideoLink(entrySong.link)
                    })
                    infoBuilder.create().show()

                }else switchtoLoading(entrySong.title,entrySong.number)

            }
        }

    }

    override fun onResume() {
        super.onResume()
    }

    override fun onRestart() {
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

//        val complist = ArrayList<Song>()

//        val s = Song(title = "Bohemian Rhapsody", number = 1, artist = "Queen", link = "https://youtu.be/fJ9rUzIMcZQ", complete = 1)
//        val r = Song(title = "I Fought the Law", number = 1, artist = "Queen", link = "https://youtu.be/fJ9rUzIMcZQ", complete = 2)
//        complist.add(s)
//        complist.add(r)

        //custom list adapter written to get stars to display properly
        //takes array list of songs

        //quick fix will edit later
        val compAdapter = songListAdapter(ArrayList(songs.filter { it.complete > 0 }),this)

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

    private fun refreshSongList(){
        println(">>> diff: $diff")
        songs.map{ println("Title: " + it.title +" complete: " + it.complete) }
        println("++++++")
        songs.map{ it.complete = getSongCompleted.getInt(it.title,0)}
        songs.map{ println("Title: " + it.title +" complete: " + it.complete) }

        var adapter = ArrayAdapter(this@SongSelectActivity,R.layout.simplerow,songs)
        songList.adapter = adapter

    }

    //change activity
    private fun switchtoLoading(songTitle:String,songNumber:Int){
        val intent = Intent(this,LoadingScreen::class.java)
        intent.putExtra(diffSend,diff)
        intent.putExtra("songle.songName",songTitle)
        intent.putExtra("songle.songNumber",songNumber)
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

                //error must fix probably not seeding properly

                shuffle(songs,java.util.Random(diff.toLong()))

                var adapter = ArrayAdapter(this@SongSelectActivity,R.layout.simplerow,songs)

                songList.adapter = adapter

                }

            override fun onFailure(statusCode: Int, headers: Array<out Header>?, throwable: Throwable?, file: File?) {
                songDownloadFail()
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
        var completed = 0

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
