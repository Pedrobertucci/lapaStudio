package c.pedrobertucci.lapachallenge.database

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

open class SharedPreference(context: Context) {

    companion object {
        const val PREFS_NAME = "lapaStudio"
        const val LIST_LOCATION = "LIST_LOCATION_USER"
    }

    private val sharedPref: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    open fun saveArrayList(list: ArrayList<LocationUser>, key: String) {
        val editor = sharedPref.edit()
        val gson = Gson()
        val json = gson.toJson(list)
        editor.putString(key, json)
        editor.apply()
    }

    open fun getArrayList(key: String): ArrayList<LocationUser> {
        val gson = Gson()
        val json = sharedPref.getString(key, null)
        val type = object : TypeToken<ArrayList<LocationUser>>() {}.type

        if(json == null) {
            return ArrayList()
        } else {
            return gson.fromJson(json, type)
        }
    }
}