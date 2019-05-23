package com.connection.hamza.com.blerx.util

fun ByteArray.toHex() = joinToString("") { String.format("%02X", (it.toInt() and 0xff)) }