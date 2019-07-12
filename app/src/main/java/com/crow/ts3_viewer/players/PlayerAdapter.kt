package com.crow.ts3_viewer.players

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.crow.ts3_viewer.R

class PlayerAdapter(private val entries: List<PlayerEntry>) : RecyclerView.Adapter<PlayerViewHolder>()
{
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        PlayerViewHolder(LayoutInflater.from(parent.context)
            .inflate(R.layout.view_player, parent, false))

    override fun getItemCount() =
        entries.count()

    override fun onBindViewHolder(holder: PlayerViewHolder, position: Int)
    {
        entries[position].let {
            holder.icon.setImageResource(it.icon)
            holder.nickname.text = it.name
            holder.country.text  = it.country
        }
    }
}