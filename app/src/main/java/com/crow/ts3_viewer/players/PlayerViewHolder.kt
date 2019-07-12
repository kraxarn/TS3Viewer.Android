package com.crow.ts3_viewer.players

import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.RecyclerView
import com.crow.ts3_viewer.R

class PlayerViewHolder(view: View) : RecyclerView.ViewHolder(view)
{
    val icon     = view.findViewById<AppCompatImageView>(R.id.image_player_icon)
    val nickname = view.findViewById<TextView>(R.id.text_player_name)
    val country  = view.findViewById<TextView>(R.id.text_player_country)
}