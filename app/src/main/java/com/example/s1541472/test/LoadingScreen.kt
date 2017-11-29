package com.example.s1541472.test

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.view.View
import com.loopj.android.http.AsyncHttpClient
import com.loopj.android.http.FileAsyncHttpResponseHandler
import cz.msebera.android.httpclient.Header
import kotlinx.android.synthetic.main.activity_loading_screen.*
import java.io.File

/**
 * @author Andrew Lindsay
 * @version 0.000001 pre-alpha
 */
class LoadingScreen : AppCompatActivity() {

    private lateinit var intentMap:Intent
    var finishDownload: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loading_screen)

        intentMap = Intent(this,MapsActivity::class.java)

        val url = "https://www.inf.ed.ac.uk/teaching/courses/cslp/data/songs"

        val intent = intent

        //Easist map is map5 difficulties start at 0 (easy)
        val diff = intent.getIntExtra("songle.difficultyTransfer",0)

        val songName = intent.getStringExtra("songle.songName")

        val diffmap = 5 - diff

        val songNumber:Int = intent.getIntExtra("songle.songNumber",0)

        intentMap.putExtra("songle.difficultyTransfer",diffmap)
        intentMap.putExtra("songle.songName",songName)


        val songNumStr:String = if(songNumber > 9) songNumber.toString() else ("0" + songNumber)

        println("${url}/${songNumStr}/map${diffmap}.kml")
        println("${url}/${songNumStr}/words.txt")

        downloadMap("${url}/${songNumStr}/map${diffmap}.kml","${url}/${songNumStr}/words.txt")

        println("Create finished")

    }

    //IRFB
    private fun downloadMap(fileUrl1:String,fileUrl2: String){

        //link locations
        //https://www.inf.ed.ac.uk/teaching/courses/cslp/data/songs/01/words.txt
        //https://www.inf.ed.ac.uk/teaching/courses/cslp/data/songs/01/map5.txt

        val client = AsyncHttpClient()
        client.get(fileUrl1, object : FileAsyncHttpResponseHandler(/* Context */this) {

            override fun onSuccess(statusCode: Int, headers: Array<Header>, response: File) {

                //add file loc to intent
                intentMap.putExtra("songle.mapFilePath",response.toString())
                println(">>> File downloaded: Maps")
                downloadlyrics(fileUrl2)
            }

            override fun onFailure(statusCode: Int, headers: Array<out Header>?, throwable: Throwable?, file: File?) {
                    println(">>> Failed to download: Map Data")
                        Snackbar.make( rootitem, "Maps Download has Failed", Snackbar.LENGTH_INDEFINITE)
                        .setAction("Retry", View.OnClickListener {
                            downloadMap(fileUrl1,fileUrl2)
                        } ).show()
            }
        })
    }


    private fun downloadlyrics(fileUrl:String){

        //link locations
        //https://www.inf.ed.ac.uk/teaching/courses/cslp/data/songs/01/words.txt
        //https://www.inf.ed.ac.uk/teaching/courses/cslp/data/songs/01/map5.txt
        val client = AsyncHttpClient()
        client.get(fileUrl, object : FileAsyncHttpResponseHandler(/* Context */this) {

            override fun onSuccess(statusCode: Int, headers: Array<Header>, response: File) {

                //add file location to intent message
                intentMap.putExtra("songle.lyricsFilePath",response.toString())
                println(response.toString())
                println(">>> File downloaded: Lyrics")
                //switch activity
                startActivity(intentMap)
            }

            override fun onFailure(statusCode: Int, headers: Array<out Header>?, throwable: Throwable?, file: File?) {

                Snackbar.make( rootitem, "Maps Download has Failed", Snackbar.LENGTH_INDEFINITE)
                        .setAction("Retry",View.OnClickListener {
                            downloadlyrics(fileUrl)
                        } ).show()


                println(">>> Failed to download: Lyrics")

            }
        })
    }

}
