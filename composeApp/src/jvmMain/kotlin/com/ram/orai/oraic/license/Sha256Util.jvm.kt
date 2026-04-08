package com.ram.orai.oraic.license

import java.security.MessageDigest

actual object Sha256Util {
    actual fun hash(input: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(input.toByteArray(Charsets.UTF_8))
        return hashBytes.joinToString("") { "%02x".format(it) }
    }
}



