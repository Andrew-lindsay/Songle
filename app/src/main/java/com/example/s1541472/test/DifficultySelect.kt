package com.example.s1541472.test

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_difficulty_select.*


class DifficultySelect : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_difficulty_select)

        easy.setOnClickListener { switchToMap() }
        medium.setOnClickListener {switchToSonglist() }

        val infoBuilder = AlertDialog.Builder(this)
        infoBuilder.setMessage("Press and hold for info on difficulty level")
                .setTitle("What do the Difficulties mean")
        //set close button for exit
        infoBuilder.setPositiveButton("close", DialogInterface.OnClickListener { dialog, id ->
            // User clicked OK button
        })


        info_question.setOnClickListener { infoBuilder.create().show() }


    }

    private fun switchToSonglist(){
        val intent = Intent(this,SongSelectActivity::class.java)
        startActivity(intent)
    }


    private fun switchToMap(){
        val intent = Intent(this,MapsActivity::class.java)
        startActivity(intent)
    }

}
