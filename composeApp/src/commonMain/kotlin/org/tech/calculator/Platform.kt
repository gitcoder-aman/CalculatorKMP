package org.tech.calculator

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform