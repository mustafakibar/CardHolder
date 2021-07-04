@file:SuppressLint("SimpleDateFormat")

package kibar.cardholder.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.widget.Toast
import com.google.gson.Gson
import java.text.SimpleDateFormat
import java.util.*

inline operator fun <reified T : Any> SharedPreferences.set(key: String, value: T) =
    with(edit()) {
        when (T::class) {
            Boolean::class -> putBoolean(key, value as Boolean)
            Float::class -> putFloat(key, value as Float)
            Int::class -> putInt(key, value as Int)
            Long::class -> putLong(key, value as Long)
            String::class -> putString(key, value as String)
            else -> {
                if (value is Set<*>) {
                    @Suppress("UNCHECKED_CAST")
                    putStringSet(key, value as Set<String>)
                } else {
                    putString(key, Gson().toJson(value))
                }
            }
        }
        commit()
    }


private val calendar by lazy { Calendar.getInstance() }
private val simpleDateFormat by lazy { SimpleDateFormat("dd-MM-yyyy HH:mm:ss a") }

fun getCurrentDateTimeAsFormatted(): String = simpleDateFormat.format(calendar.time)

fun Context.toast(message: String, length: Int = Toast.LENGTH_SHORT) =
    Toast.makeText(this, message, length).show()


fun Context.isNetworkAvailable() =
    (getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager).activeNetworkInfo?.isConnectedOrConnecting
        ?: false