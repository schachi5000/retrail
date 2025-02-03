package pro.schacher.gpsrekorder.shared.model

import io.github.dellisd.spatialk.geojson.Position

data class LatLng(val latitude: Double, val longitude: Double)


fun LatLng.toPosition() = Position(this.longitude, this.latitude)