package com.stringee.kotlin_onetoonecallsample

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.widget.Toast
import com.stringee.StringeeClient
import com.stringee.call.StringeeCall
import com.stringee.call.StringeeCall2

object Common {
                lateinit var client: StringeeClient
    var isInCall: Boolean = false
    var callsMap: HashMap<String, StringeeCall> = HashMap()
    var call2sMap: HashMap<String, StringeeCall2> = HashMap()
    const val TAG: String = "Stringee"
    const val REQUEST_PERMISSION_CALL = 1
    const val PREF_BASE = "com.stringee.softphone"
    const val PREF_ACCESS_TOKEN = "$PREF_BASE.access_token"
    const val PREF_USER_ID = "$PREF_BASE.user_id"

    fun reportMessage(context: Context, msg: String) {
        val toast = Toast.makeText(context, msg, Toast.LENGTH_SHORT)
        toast.setGravity(Gravity.CENTER, 0, 0)
        toast.show()
    }

    fun postDelay(runnable: Runnable, delayMillis: Long) {
        Handler(Looper.getMainLooper()).postDelayed(runnable, delayMillis)
    }
}
