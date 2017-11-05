package com.example.s1541472.test

import android.app.AlertDialog
import android.app.PendingIntent.getActivity
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View

import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.activity_difficulty_select.*

//git test push
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        //build info dialog box
        val infoBuilder = AlertDialog.Builder(this)
        infoBuilder.setMessage(R.string.info_text)
                .setTitle(R.string.welcome_title)
        //set ok button for exit
        infoBuilder.setPositiveButton(R.string.ok, DialogInterface.OnClickListener { dialog, id ->
            // User clicked OK button
        })

        val info_box = infoBuilder.create()

        //equivalent statements
//        fab.setOnClickListener(object : View.OnClickListener {
//            override fun onClick(v: View?) {
//                switchToMap()
//            }
//        })

        //set button presses
        info.setOnClickListener { info_box.show()}
        Bmain.setOnClickListener { switchToDifficultySelect() }


        /* Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                 .setAction("Action", null).show()
    */
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun switchToDifficultySelect(){
        val intent = Intent(this,DifficultySelect::class.java)
        startActivity(intent)
    }
}
