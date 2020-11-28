package com.ms8.halloweencontrolpanel.database.objects

import android.annotation.SuppressLint
import android.content.Context
import android.util.ArrayMap
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap
import kotlin.math.PI

@Entity(tableName = "devices_table")
open class Device(
    @ColumnInfo(name = "device_name")
    var name : String,
    @ColumnInfo(name = "device_group")
    val groupName: String,
    @PrimaryKey(autoGenerate = false)
    val uid : String,
    var pin: Int = -1
) {
    @ColumnInfo(name = "device_state")
    var state = State.OFF

    @SuppressLint("SimpleDateFormat")
    open fun getFirebaseObject() : MutableMap<String, Any?> {
       val map : MutableMap<String, Any?> = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
           ArrayMap()
       } else {
           HashMap()
       }

        return map.apply {
            put(PIN, pin)
            put(NAME, name)
            put(GROUP_NAME, groupName)
            put(LAST_UPDATED, SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().time))
        }
    }

    open fun getVariableDescription(context: Context, id: Int) : String = ""

    open fun getState() : String = state.name

    override fun equals(other: Any?): Boolean {
        return other is Device && other.uid == uid
    }

    companion object {
        const val NAME = "name"
        const val GROUP_NAME = "groupName"
        const val LAST_UPDATED = "lastUpdated"
        const val PIN = "pin"

        const val PIN_A0 = 88
    }

    enum class State {
        ON, OFF
    }
}
