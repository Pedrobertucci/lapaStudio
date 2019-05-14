package c.pedrobertucci.lapachallenge.Activity

import c.pedrobertucci.lapachallenge.database.LocationUser
import junit.framework.Assert.assertEquals
import org.junit.After
import org.junit.Before
import org.junit.Test

class MapsActivityTest {

    var locationUser1: LocationUser?=null
    var locationUser2: LocationUser?=null

    @Before
    fun setUp() {
        locationUser1 = LocationUser(41.1740635,-8.5851608)
        locationUser2 = LocationUser(41.1546807,-8.700916)
    }

    @Test
    fun `Distance for home to Job`() {
        val returnDistance = MapsActivity.calculateDistance(locationUser1!!.latitude, locationUser1!!.longitude,
            locationUser2!!.latitude, locationUser2!!.longitude)

        assertEquals(returnDistance, 8.5)
    }

    @After
    fun tearDown() {
    }
}