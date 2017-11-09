package com.example.s1541472.test

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.view.View
import kotlinx.android.synthetic.main.activity_loading_screen.*


class LoadingScreen : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loading_screen)


       Snackbar.make( rootitem, "Maps Download has Failed", Snackbar.LENGTH_INDEFINITE)
                .setAction("Retry", MyUndoListener()).show()

    }

    inner class MyUndoListener : View.OnClickListener {

        override fun onClick(v: View) {

            // Code to undo the user's last action
        }
    }
}
