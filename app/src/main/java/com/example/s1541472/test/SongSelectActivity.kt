package com.example.s1541472.test

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import kotlinx.android.synthetic.main.activity_song_select.*
import android.widget.Toast
import android.widget.TextView
import android.widget.AdapterView.OnItemClickListener
import android.content.ActivityNotFoundException
import android.net.Uri


class SongSelectActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_song_select)

        var songs = ArrayList<Song>()

        for(i in 1..12){
            val song1 = Song(title = "${i}", number = 1, artist = "me", link = "None", complete = 0)
            songs.add(song1)
        }

        val song1 = Song(title = "Bohemian Rhapsody", number = 1, artist = "Queen", link = "https://youtu.be/fJ9rUzIMcZQ", complete = 1)
        songs.add(5,song1)


        val adapter = ArrayAdapter(this,R.layout.simplerow,songs)

        songList.adapter = adapter
        val infoBuilder = AlertDialog.Builder(this)


        songList.onItemClickListener = object: OnItemClickListener {
            override fun onItemClick(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                val entrySong = songs[p2]

                if (entrySong.complete == 1){

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
                    infoBuilder.setNeutralButton("Listen to Song",DialogInterface.OnClickListener { dialog, id ->
                        watchVideoLink(entrySong.link)
                    })
                    infoBuilder.create().show()

                }else switchtoMap()

            }
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
}
