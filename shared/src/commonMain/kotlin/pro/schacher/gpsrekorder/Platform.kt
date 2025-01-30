package pro.schacher.gpsrekorder

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform