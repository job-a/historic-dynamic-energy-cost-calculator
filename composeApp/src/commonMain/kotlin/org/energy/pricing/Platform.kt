package org.energy.pricing

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform