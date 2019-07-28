package com.crow.ts3_viewer

import android.content.Context
import com.crow.ts3_viewer.ts3.Ts3Data
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.list
import java.io.File

class Config(private val context: Context)
{
    val json = Json(JsonConfiguration.Stable)

    val serversFile
        get() = File(context.filesDir, "servers.json")

    fun load(): List<Ts3Data> =
        json.parse(Ts3Data.serializer().list, serversFile.readText())

    fun save(data: List<Ts3Data>) =
        serversFile.writeText(json.stringify(Ts3Data.serializer().list, data))
}