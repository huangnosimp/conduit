package vn.io.huangnosimp.hashing

import java.security.MessageDigest

fun String.toSha1(): String = toByteArray(Charsets.UTF_8).toSha1()

fun ByteArray.toSha1(): String =
    MessageDigest
        .getInstance("SHA-1")
        .digest(this)
        .joinToString("") { "%02x".format(it.toInt() and 0xff) }
