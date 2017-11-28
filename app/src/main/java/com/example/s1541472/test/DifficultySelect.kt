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
import java.lang.Math


class DifficultySelect : AppCompatActivity() {

   val diffMessage:String = "songle.difficultySet"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_difficulty_select)

        /*
        Difficulty  Number
        easy        0
        medium      1
        hard        2
        extra_hard  3
        extreme     4
        */

        //diff buttons
        easy.setOnClickListener { switchToSonglist(0) }
        medium.setOnClickListener{ switchToSonglist(1) }
        hard.setOnClickListener { switchToSonglist(2) }
        extra_hard.setOnClickListener { switchToLoading() }
        Extreme.setOnClickListener { switchToSonglist(4) }
        random.setOnClickListener{switchToSonglist(randDiff())}

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

    private fun switchToSonglist(diff:Int){
        val intent = Intent(this,SongSelectActivity::class.java)
        intent.putExtra(diffMessage,diff)
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
                .setMessage("Press and hold on a difficulty levels button for more information on what that difficulty contains.")
                .setTitle("What do the Difficulties mean?").create().show()
    }

    private fun randDiff():Int{
        return (Math.random()*10).toInt() % 5
    }

}
