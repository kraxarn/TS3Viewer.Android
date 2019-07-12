package com.crow.ts3_viewer.info

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.crow.ts3_viewer.R
import com.crow.ts3_viewer.players.PlayerAdapter

class InfoAdapter(private val entries: List<InfoEntry>) : RecyclerView.Adapter<InfoViewHolder>()
{
    private lateinit var context: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InfoViewHolder
    {
        context = parent.context

        return InfoViewHolder(LayoutInflater.from(context)
            .inflate(R.layout.view_info, parent, false))
    }

    override fun getItemCount() =
        entries.count()

    override fun onBindViewHolder(holder: InfoViewHolder, position: Int)
    {
        val entry = entries[position]

        holder.channel.text = entry.channel
        holder.playerCount.text = entry.playerCount

        holder.players.let {
            it.adapter       = PlayerAdapter(entry.players)
            it.layoutManager = LinearLayoutManager(context)
        }
    }
}