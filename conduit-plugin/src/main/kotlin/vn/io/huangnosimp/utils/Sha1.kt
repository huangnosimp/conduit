package vn.io.huangnosimp.utils

import java.security.MessageDigest

fun String.toSha1(): String = this.toByteArray().toSha1()

fun ByteArray.toSha1(): String{
    val bytes = MessageDigest.getInstance("SHA-1").digest(this)
    return bytes.joinToString("") { "%02x".format(it) }
}