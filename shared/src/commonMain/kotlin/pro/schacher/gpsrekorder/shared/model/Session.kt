package pro.schacher.gpsrekorder.shared.model

data class Session(val id: String, val path: List<Location> = emptyList())