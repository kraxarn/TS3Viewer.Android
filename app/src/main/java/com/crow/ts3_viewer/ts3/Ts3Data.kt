package com.crow.ts3_viewer.ts3

import android.content.Context
import com.crow.ts3_viewer.servers.ServersEntry
import kotlinx.serialization.Serializable

@Serializable
data class Ts3Data(val host: String, val name: String, val queryPort: Int)
{
    fun toEntry(context: Context): ServersEntry =
        Ts3(host, name, queryPort).apply {
            connect()
        }.toEntry(context, this)
}