package c.pedrobertucci.lapachallenge.database


class LocationUser constructor(latitude: Double,
                               longitude: Double) {
    var latitude: Double = 0.0
    var longitude: Double = 0.0

    init {
        this.latitude = latitude
        this.longitude = longitude
    }
}