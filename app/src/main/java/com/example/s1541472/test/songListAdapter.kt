package com.example.s1541472.test

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.BaseAdapter
import kotlinx.android.synthetic.main.custom_list_adp.view.*
import kotlinx.android.synthetic.main.drawr_list_item.view.*
import java.text.FieldPosition

/**
 * Created by s1541472 on 08/11/17.
 */
class songListAdapter(val songs:ArrayList<Song>,val cxt:Context) :ArrayAdapter<Song>(cxt,0,songs) {

    override fun getCount(): Int {
        return songs.size
    }

    override fun getItem(position: Int): Song {
        return songs[position]
    }

    override fun getView(position:Int, convertview: View?, parent: ViewGroup): View {

        var conView:View

        val songNow = getItem(position)

        if(convertview == null){
            conView = LayoutInflater.from(cxt).inflate(R.layout.custom_list_adp, parent, false)
            conView.songTitle.setText(songNow.title)

            val stars = arrayOf(conView.star1,conView.star2,conView.star3,conView.star4,conView.star5)

            val num_stars = if (songNow.complete > 0) songNow.complete - 1  else 0

            for(i in 0..num_stars){
                stars[i].setImageResource(android.R.drawable.btn_star_big_on)
            }

            return conView

        }
        return convertview
    }
}