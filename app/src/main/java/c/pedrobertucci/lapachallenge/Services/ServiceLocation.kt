package c.pedrobertucci.lapachallenge.Services

import android.Manifest
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.os.Binder
import android.app.PendingIntent
import android.widget.Toast
import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import android.util.Log
import c.pedrobertucci.lapachallenge.Activity.MapsActivity
import c.pedrobertucci.lapachallenge.R
import c.pedrobertucci.lapachallenge.database.SharedPreference
import android.os.HandlerThread
import android.os.Handler
import android.os.Process.THREAD_PRIORITY_BACKGROUND



class ServiceLocation : Service() {
    private var notificationManager: NotificationManager? = null
    private val NOTIFICATION = 1
    private val mBinder = LocalBinder()
    private var mHandler: Handler? = null
    private var mHandlerThread: HandlerThread? = null


    inner class LocalBinder : Binder() {
        internal val service: ServiceLocation
            get() = this@ServiceLocation
    }

    private val locationBackground =  Runnable{

        kotlin.run {
            val location =
                c.pedrobertucci.lapachallenge.database.LocationUser(MapsActivity.latitude!!, MapsActivity.longitude!!)
            MapsActivity.LIST_LOCATION_USER!!.add(location)
            SharedPreference(this).saveArrayList(MapsActivity.LIST_LOCATION_USER, SharedPreference.LIST_LOCATION)
        }
    }

    override fun onCreate() {
        Log.i(MapsActivity.TAG, "ServiceLocation  on")
        mHandlerThread = HandlerThread(
            "LocatorService", THREAD_PRIORITY_BACKGROUND)
        mHandlerThread!!.start()

        mHandler = Handler(mHandlerThread!!.looper)

        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        showNotification()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.i(MapsActivity.TAG, "LocalService: Received start id $startId: $intent")
        mHandler!!.post(locationBackground)
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        notificationManager!!.cancel(NOTIFICATION)
        Toast.makeText(this, getString(R.string.local_service_stopped), Toast.LENGTH_SHORT).show()
    }

    override fun onBind(intent: Intent): IBinder? {
        return mBinder
    }


    private fun showNotification() {
        val text = getText(R.string.local_service_started)

        val contentIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MapsActivity::class.java), 0
        )

        val notification = Notification.Builder(this)
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setTicker(text)
            .setWhen(System.currentTimeMillis())
            .setContentTitle(getText(R.string.local_service_label))
            .setContentText(text)
            .setContentIntent(contentIntent)
            .build()

        notificationManager!!.notify(NOTIFICATION, notification)
    }
}