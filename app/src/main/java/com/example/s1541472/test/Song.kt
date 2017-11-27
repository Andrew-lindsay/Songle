package com.example.s1541472.test

/**
 * Created by s1541472 on 05/11/17.
 */

class Song(val number: Int =-1, val artist: String="", val title: String="", val link:String="",var complete:Int = 5) {

    override fun toString(): String {
        return if( complete==0 ) "?" else title
    }
}