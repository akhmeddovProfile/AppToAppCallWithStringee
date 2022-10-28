package com.stringee.kotlin_onetoonecallsample

import android.content.Context
import android.content.SharedPreferences

object PrefUtils {
    private var preferences: SharedPreferences? = null
    private var instance: PrefUtils? = null

    fun getInstance(context: Context): PrefUtils? {
        if (instance == null) {
            instance = PrefUtils
        }
        if (preferences == null) {
            preferences = context.getSharedPreferences(Common.PREF_BASE, Context.MODE_PRIVATE)
        }
        return instance
    }

    fun getInt(key: String?, defValue: Int): Int {
        return preferences!!.getInt(key, defValue)
    }

    fun getString(key: String?, defValue: String?): String? {
        return preferences!!.getString(key, defValue)
    }

    fun getBoolean(key: String?, defValue: Boolean): Boolean {
        return preferences!!.getBoolean(key, defValue)
    }

    fun getLong(key: String?, defValue: Long): Long {
        return preferences!!.getLong(key, defValue)
    }

    fun putInt(key: String?, defValue: Int) {
        val editor = preferences!!.edit()
        editor.putInt(key, defValue)
        editor.commit()
    }

    fun putString(key: String?, defValue: String?) {
        val editor = preferences!!.edit()
        editor.putString(key, defValue)
        editor.commit()
    }

    fun putBoolean(key: String?, defValue: Boolean) {
        val editor = preferences!!.edit()
        editor.putBoolean(key, defValue)
        editor.commit()
    }

    fun putLong(key: String?, defValue: Long) {
        val editor = preferences!!.edit()
        editor.putLong(key, defValue)
        editor.commit()
    }

    fun clearData() {
        val editor = preferences!!.edit()
        editor.clear()
        editor.commit()
    }

}