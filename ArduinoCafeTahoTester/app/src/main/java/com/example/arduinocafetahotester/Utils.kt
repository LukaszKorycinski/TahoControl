package com.example.arduinocafetahotester

import java.nio.ByteBuffer


fun float2ByteArray(value: Float): ByteArray? {
    return ByteBuffer.allocate(4).putFloat(value).array()
}

fun intToBytes(i: Int): ByteArray =
    ByteBuffer.allocate(Int.SIZE_BYTES).putInt(i).array()