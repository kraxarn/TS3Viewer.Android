package com.crow.ts3_viewer.servers

import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.RecyclerView
import com.crow.ts3_viewer.MainActivity
import com.crow.ts3_viewer.R
import com.crow.ts3_viewer.ts3.Ts3

class ServersViewHolder(view: View) : RecyclerView.ViewHolder(view)
{
    val imageLogo   = view.findViewById<AppCompatImageView>(R.id.image_server)
    val textName    = view.findViewById<TextView>(R.id.text_server_name)
    val textIp      = view.findViewById<TextView>(R.id.text_server_ip)
    val textPlayers = view.findViewById<TextView>(R.id.text_server_players)

    init {
        view.setOnClickListener {
            // TODO: This ignores port settings
            MainActivity.showServerInfo(Ts3(textIp.text.toString()))
        }
    }
}