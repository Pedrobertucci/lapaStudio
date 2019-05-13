package c.pedrobertucci.lapachallenge.Activity

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.util.Log
import c.pedrobertucci.lapachallenge.database.SharedPreference
import c.pedrobertucci.lapachallenge.R
import c.pedrobertucci.lapachallenge.Services.ServiceLocation
import c.pedrobertucci.lapachallenge.database.LocationUser

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, SensorEventListener {

    private lateinit var mMap: GoogleMap
    private lateinit var locationManager: LocationManager
    private lateinit var accelerometer: Sensor
    private lateinit var sensorManager: SensorManager

    companion object {
        const val PERMISSION_REQUEST_ACCESS_FINE_LOCATION = 100
        const val TAG = "LAPA"
        var SENSOR_X: Float = 0.0f
        var SENSOR_Y: Float = 0.0f
        var SENSOR_Z: Float = 0.0f
        var LAST_UPDATE: Long? = 0
        var latitude: Double?= 0.0
        var longitude: Double?= 0.0
        val LIST_LOCATION_USER: ArrayList<LocationUser> ?= ArrayList()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        sensorManager  = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)

        ServiceLocation()

        getLocationUser()

        viewLocationUser()

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    private fun viewLocationUser() {
        Thread(Runnable {
            this@MapsActivity.runOnUiThread {
                getValuesForUser()
            }
        })
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        getValuesForUser()
    }

    private fun getValuesForUser() {
        val listLocation = SharedPreference(this).getArrayList(SharedPreference.LIST_LOCATION)
        val sizeList = listLocation.size -1

        if(listLocation.size > 0) {
            val lastThreeLocations = getThreeLocationsForMyLocation(listLocation, sizeList)
            var position: Int?=0

            for(item in lastThreeLocations) {
                mMap.addMarker(MarkerOptions().position(LatLng(item.latitude, item.longitude)).title("My Location - $position"))
                position = position!! + 1
            }

            mMap.moveCamera(CameraUpdateFactory.newLatLng(LatLng(
                listLocation[sizeList].latitude,
                listLocation[sizeList].longitude)))
        }
    }

    private fun getThreeLocationsForMyLocation(listLocationUsers: ArrayList<LocationUser>, size: Int): ArrayList<LocationUser> {

        val valuesArrayList: ArrayList<LocationUser>?= ArrayList()

        valuesArrayList!!.add(listLocationUsers[size])
        if(listLocationUsers.size > 2) {
            valuesArrayList.add(listLocationUsers[size-1])
            valuesArrayList.add(listLocationUsers[size-2])
        }

        return valuesArrayList
    }

    private fun getLocationUser() {

        locationManager = (getSystemService(LOCATION_SERVICE) as LocationManager?)!!

        val locationListener = object : LocationListener {
            override fun onLocationChanged(location: Location?) {
                latitude = location!!.latitude
                longitude = location.longitude
                Log.i(MapsActivity.TAG, "onLocationChanged -> Latitute: $latitude ; Longitute: $longitude")
            }

            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
                Log.i(MapsActivity.TAG, "onStatusChanged -> PROVIDER: $provider ; STATUS: $status ; EXTRAS: $extras")
            }

            override fun onProviderEnabled(provider: String?) {
                Log.i(MapsActivity.TAG, "onProviderEnabled ->  onProviderEnabled PROVIDER: $provider")
            }

            override fun onProviderDisabled(provider: String?) {
                Log.i(MapsActivity.TAG, "onProviderDisabled -> PROVIDER: $provider")
            }
        }

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSION_REQUEST_ACCESS_FINE_LOCATION
            )
            return
        }
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0L, 0f, locationListener)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_ACCESS_FINE_LOCATION) {
            when (grantResults[0]) {
                PackageManager.PERMISSION_GRANTED -> getLocationUser()
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, value: Int) {
    }

    override fun onSensorChanged(event: SensorEvent?) {
        val sensor = event!!.sensor

        if(sensor.type == Sensor.TYPE_ACCELEROMETER) {
           val returnSpeed = calculateVelocity(event.values[0], event.values[1], event.values[2])

            Log.i(MapsActivity.TAG, "Speed -> $returnSpeed")

            if(returnSpeed!! > 2) {
                saveLocationUser()
            }
        }
    }

    private fun calculateVelocity(x: Float, y: Float, z: Float): Float? {
        val timeNow = System.currentTimeMillis()
        var speed: Float?= 0.0f

        if((timeNow - LAST_UPDATE!!) > 700){
            val differentTime = (timeNow - LAST_UPDATE!!)
            LAST_UPDATE = timeNow

            speed = Math.abs(x+y+z - SENSOR_X - SENSOR_Y - SENSOR_Z) / differentTime * 10000

            SENSOR_X = x
            SENSOR_Y = y
            SENSOR_Z = z
        }
        return speed
    }

    private fun saveLocationUser(){
        val location = c.pedrobertucci.lapachallenge.database.LocationUser(latitude!!, longitude!!)
        LIST_LOCATION_USER!!.add(location)
        SharedPreference(this).saveArrayList(LIST_LOCATION_USER, SharedPreference.LIST_LOCATION)
    }

    private fun calculateDistance(latitude1: Double, longitude1: Double, latitude2: Double, longitude2: Double): Double? {
            val theta = longitude1 - longitude2
            var dist = Math.sin(valueInPI(latitude1)!!) * Math.sin(valueInPI(latitude2)!!) +
                    Math.cos(valueInPI(latitude1)!!) * Math.cos(valueInPI(latitude2)!!) * Math.cos(valueInPI(theta)!!)

            dist = Math.acos(dist)
            dist = valueInNormal(dist)!!
            dist *= 60 * 1.1515
            dist *= 1.609344
            return (dist)
    }

    private fun valueInPI(value: Double): Double? {
         return (value * Math.PI / 180.0)
    }

    private fun valueInNormal(value: Double): Double? {
        return (value * 180 / Math.PI)
    }
}
