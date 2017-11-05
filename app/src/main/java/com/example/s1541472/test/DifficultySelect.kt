package com.example.s1541472.test

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_difficulty_select.*


class DifficultySelect : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_difficulty_select)

        easy.setOnClickListener { switchToMap() }

    }


    private fun switchToMap(){
        val intent = Intent(this,MapsActivity::class.java)
        startActivity(intent)
    }

}
