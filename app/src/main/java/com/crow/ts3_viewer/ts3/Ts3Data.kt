package com.crow.ts3_viewer.ts3

import kotlinx.serialization.Serializable

@Serializable
data class Ts3Data(private val host: String, private val queryPort: Int)