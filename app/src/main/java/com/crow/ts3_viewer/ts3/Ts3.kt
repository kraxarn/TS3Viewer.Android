package com.crow.ts3_viewer.ts3

import android.content.Context
import com.crow.ts3_viewer.R
import com.crow.ts3_viewer.info.InfoEntry
import com.crow.ts3_viewer.players.PlayerEntry
import com.crow.ts3_viewer.servers.ServersEntry
import com.github.theholywaffle.teamspeak3.TS3Api
import com.github.theholywaffle.teamspeak3.TS3Config
import com.github.theholywaffle.teamspeak3.TS3Query
import com.github.theholywaffle.teamspeak3.api.exception.TS3ConnectionFailedException
import com.github.theholywaffle.teamspeak3.api.wrapper.Channel
import com.github.theholywaffle.teamspeak3.api.wrapper.Client

class Ts3(private val host: String, private val port: Int = 9987,
          private val queryPort: Int = 10011, private val name: String = host)
{
    private val config = TS3Config().apply {
        setHost(host)
    }
    private val query  = TS3Query(config)

    private var api: TS3Api? = null

    fun connect(): Boolean
    {
        try {
            query.connect()
        } catch (e: TS3ConnectionFailedException) {
            return false
        }

        if (query.isConnected)
            api = query.api.apply {
                selectVirtualServerById(1)
            }

        return query.isConnected
    }

    fun exit()
    {
        query.exit()
    }

    fun toEntry(context: Context): ServersEntry =
        ServersEntry(context.getDrawable(R.drawable.ic_server_network)!!, api!!.serverInfo.name, host,
            "${api!!.clients.count()}/${api!!.serverInfo.maxClients}")

    private fun getClientIcon(client: Client): Int
    {
        /*
         * Client icons in order:
         * 1. Away          (sleep)
         * 2. Speaker muted (volume_off)
         * 3. Mic muted     (microphone-off)
         * 4. Normal        (volume_high)
         */

        // Away
        if (client.isAway)
            return R.drawable.ic_sleep

        // Speaker muted
        if (client.isOutputMuted)
            return R.drawable.ic_volume_off

        // Mic muted
        if (client.isInputMuted)
            return R.drawable.ic_mic_off

        // All good
        return R.drawable.ic_volume_up
    }

    private val Client.entryIcon
        get() = getClientIcon(this)

    val channels: List<InfoEntry>
        get() {
            class TempEntry(val channel: Channel, val players: MutableList<PlayerEntry> = mutableListOf())
            val entries = mutableListOf<TempEntry>()

            // Add all channels
            for (channel in api!!.channels)
            {
                if (channel.totalClients > 0)
                    entries.add(TempEntry(channel))
            }

            // Add clients to their respective channel
            for (client in api!!.clients)
            {
                // Ignore self
                //if (client.nickname == "Unknown")
                //    continue

                entries.first { e ->
                    e.channel.id == client.channelId
                }.players.add(PlayerEntry(client.nickname, client.entryIcon, client.country))
            }

            // Create final list
            return entries.map { e ->
                InfoEntry(e.channel.name, "${e.channel.totalClients} client${if (e.channel.totalClients != 1) "s" else ""}", e.players)
            }
        }
}