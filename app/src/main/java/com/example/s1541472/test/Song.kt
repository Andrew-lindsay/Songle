package com.example.s1541472.test

/**
 * Created by s1541472 on 05/11/17.
 */

class Song(val number: Int =-1, val artist: String="", val title: String="", val link:String="",var complete:Int = 0) {

    //used for display in the list adapter displayed on the song select screen
    override fun toString(): String {
        return if( complete==0 ) "?" else title
    }
}