package com.crow.ts3_viewer.servers

import android.view.View
import android.widget.PopupMenu
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatImageButton
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.RecyclerView
import com.crow.ts3_viewer.MainActivity
import com.crow.ts3_viewer.R
import com.crow.ts3_viewer.ts3.Ts3
import com.crow.ts3_viewer.ts3.Ts3Data
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration

class ServersViewHolder(view: View) : RecyclerView.ViewHolder(view)
{
    val imageLogo   = view.findViewById<AppCompatImageView>(R.id.image_server)!!
    val textName    = view.findViewById<TextView>(R.id.text_server_name)!!
    val textIp      = view.findViewById<TextView>(R.id.text_server_ip)!!
    val textPlayers = view.findViewById<TextView>(R.id.text_server_players)!!

    private val main = view.context as MainActivity

    private val ts3
        get() = Ts3(textIp.text.toString(), textName.text.toString(), main.getServerData(textName.text.toString())?.queryPort ?: 1011)

    init {
        view.setOnClickListener {
            main.showServerInfo(ts3)
        }
        val options = view.findViewById<AppCompatImageButton>(R.id.button_options)
        options.setOnClickListener {
            PopupMenu(view.context, options).let {
                it.menuInflater.inflate(R.menu.menu_server_options, it.menu)
                it.setOnMenuItemClickListener {item ->
                    when (item.itemId)
                    {
                        R.id.menu_server_json -> AlertDialog.Builder(main)
                            .setTitle("Server Data")
                            .setMessage(Json(JsonConfiguration.Stable).stringify(Ts3Data.serializer(), ts3.toData()))
                            .setPositiveButton("OK", null)
                            .show()
                    }
                    true
                }
                it.show()
            }
        }
    }
}