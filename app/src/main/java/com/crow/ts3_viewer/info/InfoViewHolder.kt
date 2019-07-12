package com.crow.ts3_viewer.info

import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.crow.ts3_viewer.R

class InfoViewHolder(view: View) : RecyclerView.ViewHolder(view)
{
    val channel     = view.findViewById<TextView>(R.id.text_info_channel)
    val playerCount = view.findViewById<TextView>(R.id.text_info_player_count)
    val chevron     = view.findViewById<ImageButton>(R.id.button_info_chevron)
    val players     = view.findViewById<RecyclerView>(R.id.view_info_players)

    init {
        chevron.setOnClickListener {
            players.visibility.let {
                players.visibility = if (it == View.GONE) View.VISIBLE else View.GONE
                chevron.setImageResource(if (it == View.GONE) R.drawable.ic_chevron_up
                    else R.drawable.ic_chevron_down)
            }
        }
    }
}