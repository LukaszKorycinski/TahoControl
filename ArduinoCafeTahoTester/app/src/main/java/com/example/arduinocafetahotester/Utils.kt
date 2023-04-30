package com.example.arduinocafetahotester

import java.nio.ByteBuffer


fun float2ByteArray(value: Float): ByteArray? {
    return ByteBuffer.allocate(4).putFloat(value).array()
}

fun intToBytes(i: Int): ByteArray =
    ByteBuffer.allocate(Int.SIZE_BYTES).putInt(i).array()

var vendorIds: List<Int> = listOf(9025, 5824, 1003, 7855, 3368, 1155, 1191, 1191, 6790, 6790, 6790, 1659, 1659, 1659, 1659, 1659, 1659, 1659, 4292, 4292, 4292, 1027, 1027, 1027, 1027, 1027,)


