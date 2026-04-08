package com.ram.orai.oraic

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform