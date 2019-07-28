package com.crow.ts3_viewer.servers

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.crow.ts3_viewer.R

class ServersAdapter(private val entries: List<ServersEntry>) : RecyclerView.Adapter<ServersViewHolder>()
{
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServersViewHolder
    {
        val inflater = LayoutInflater.from(parent.context)
        val entry = inflater.inflate(R.layout.view_server, parent, false)

        return ServersViewHolder(entry)
    }

    override fun getItemCount() =
        entries.count()

    override fun onBindViewHolder(holder: ServersViewHolder, position: Int)
    {
        val entry = entries[position]

        // TODO: Use setImageResource(Int) instead
        holder.imageLogo.setImageDrawable(entry.icon)
        holder.textName.text    = entry.data.name
        holder.textIp.text      = entry.data.host
        holder.textPlayers.text = entry.players
    }
}