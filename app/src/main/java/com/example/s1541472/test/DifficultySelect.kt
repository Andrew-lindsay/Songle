package com.example.s1541472.test

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_difficulty_select.*


class DifficultySelect : AppCompatActivity() {



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_difficulty_select)

        easy.setOnClickListener { switchToMap() }
        medium.setOnClickListener {switchToSonglist() }
        hard.setOnClickListener {switchToLoading()}

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_difficulty, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        return when (item.itemId) {
            R.id.info ->  {
                    infoShow()
                true}
            else -> super.onOptionsItemSelected(item)
        }
    }


    private fun switchToSonglist(){
        val intent = Intent(this,SongSelectActivity::class.java)
        startActivity(intent)
    }


    private fun switchToMap(){
        val intent = Intent(this,MapsActivity::class.java)
        startActivity(intent)
    }

    private fun switchToLoading(){
        val intent = Intent(this,LoadingScreen::class.java)
        startActivity(intent)
    }

    private fun infoShow(){

        var infoBuilder = AlertDialog.Builder(this).setPositiveButton("Ok", {_,_ ->})
                .setMessage("Press and hold for info on difficulty level")
                .setTitle("What do the Difficulties mean").create().show()
    }

}
