package com.ms8.halloweencontrolpanel.database

import android.util.ArrayMap
import android.util.Log
import androidx.databinding.ObservableField
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.ms8.halloweencontrolpanel.database.objects.Device
import com.ms8.halloweencontrolpanel.database.objects.LanternDevice
import com.ms8.halloweencontrolpanel.database.objects.SpiderDropDevice
import com.ms8.halloweencontrolpanel.database.objects.SpiderDropDevice.Companion.CURRENT_SENSE_PIN
import com.ms8.halloweencontrolpanel.database.objects.SpiderDropDevice.Companion.DROP_MOTOR_PIN
import com.ms8.halloweencontrolpanel.database.objects.SpiderDropDevice.Companion.HANG_TIME
import com.ms8.halloweencontrolpanel.database.objects.SpiderDropDevice.Companion.RETRACT_MOTOR_PIN
import com.ms8.halloweencontrolpanel.database.objects.SpiderDropDevice.Companion.SPIDER_STATE
import com.ms8.halloweencontrolpanel.database.objects.SpiderDropDevice.Companion.STAY_DROPPED
import kotlin.Exception

object FirebaseDao {
    val devices: ObservableField<MutableMap<String, MutableList<Device>>> = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
        ObservableField(ArrayMap())
    } else {
        ObservableField(HashMap())
    }

    /* Public Accessors */
    fun getDevice(uid: String, groupName:String? = null): Device? {
        when (groupName) {
            null -> {
                devices.get()?.keys?.forEach { key ->

                    Log.d("TEST", "Checking $key group for $uid")
                    devices.get()?.get(key)?.forEach { device ->
                        Log.d("TEST", "\t${device.uid} | $uid")
                        if (device.uid == uid)
                            return device
                    }
                }
                return null
            }
            else -> {
                devices.get()?.get(groupName)?.forEach { device ->
                    if (device.uid == uid)
                        return device
                }
                return null
            }
        }
    }

    fun updateDevice(device: Device) {
        FirebaseDatabase.getInstance().getReference("devices").child(device.uid)
                .setValue(device.getFirebaseObject())
    }

    fun updateDevice(uid: String, map: MutableMap<String, Any?>) {
        FirebaseDatabase.getInstance().getReference("devices").child(uid)
                .setValue(map)
    }

    fun listenForDevices(callback: DeviceListCallback?) {
        FirebaseDatabase.getInstance().getReference("devices").addValueEventListener(
                object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        devices.set(parseDeviceListSnapshot(snapshot))
                        devices.get()?.let { callback?.onDeviceListChanged(it) }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        callback?.onDeviceListError(error.toException())
                    }
                }
        )
    }

    fun listenToValue(value: String, deviceUID: String, listener: ValueEventListener) {
        FirebaseDatabase.getInstance().getReference("devices").child(deviceUID)
                .child(value)
                .addValueEventListener(listener)
    }

    fun stopListeningToValue(value: String, deviceUID: String, listener: ValueEventListener) {
        FirebaseDatabase.getInstance().getReference("devices").child(deviceUID)
                .child(value)
                .removeEventListener(listener)
    }

    fun sendCommand(command: String, deviceUID: String) {
        FirebaseDatabase.getInstance().getReference("command").child(deviceUID)
                .setValue(command)
    }

    /* Helper Functions */
    private fun parseDeviceListSnapshot(snapshot: DataSnapshot): MutableMap<String, MutableList<Device>> {
        val newMap : MutableMap<String, MutableList<Device>> =
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT)
                    ArrayMap()
                else
                    HashMap()

        snapshot.children.forEach {deviceSnapshot ->
            Log.d(TAG, "handleDevicesSnapshot - ${deviceSnapshot.key}")
            if (deviceSnapshot.value is Map<*,*>) {
                try {
                    val mapVal = deviceSnapshot.value as Map<*, *>
                    when (mapVal["groupName"]) {
                        LanternDevice.GroupName ->
                        {
                            val name = mapVal["name"] as String
                            val uid = deviceSnapshot.key ?: throw Exception("Invalid uid")

                            if (!newMap.containsKey(LanternDevice.GroupName))
                                newMap[LanternDevice.GroupName] = ArrayList()

                            newMap[LanternDevice.GroupName]?.add(
                                    LanternDevice(name, uid,
                                        (mapVal[Device.PIN] as Number).toInt(),
                                        (mapVal[LanternDevice.MIN_BRIGHTNESS] as Number).toInt(),
                                        (mapVal[LanternDevice.MAX_BRIGHTNESS] as Number).toInt(),
                                        (mapVal[LanternDevice.SMOOTHING] as Number).toInt(),
                                        (mapVal[LanternDevice.RAMP_DELAY] as Number).toInt(),
                                        (mapVal[LanternDevice.DROP_DELAY] as Number).toInt(),
                                        (mapVal[LanternDevice.DROP_VALUE] as Number).toInt(),
                                        (mapVal[LanternDevice.FLICKER_DELAY_MIN] as Number).toInt(),
                                        (mapVal[LanternDevice.FLICKER_DELAY_MAX] as Number).toInt()
                                            )
                            )
                        }

                        SpiderDropDevice.GroupName ->
                        {
                            val name = mapVal["name"] as String
                            val uid = deviceSnapshot.key ?: throw Exception("Invalid uid")

                            if (!newMap.containsKey(SpiderDropDevice.GroupName))
                                newMap[SpiderDropDevice.GroupName] = ArrayList()

                            newMap[SpiderDropDevice.GroupName]?.add(
                                    SpiderDropDevice(name, uid,
                                            (mapVal[Device.PIN] as Number).toInt(),
                                            (mapVal[HANG_TIME] as Number).toInt(),
                                            SpiderDropDevice.Companion.STATE.valueOf(mapVal[SPIDER_STATE] as String),
                                            (mapVal[STAY_DROPPED] as Boolean),
                                            (mapVal[DROP_MOTOR_PIN] as Number).toInt(),
                                            (mapVal[CURRENT_SENSE_PIN] as Number).toInt(),
                                            (mapVal[RETRACT_MOTOR_PIN] as Number).toInt()
                                            )
                            )
                        }
                        else -> throw Exception("Unknown device group: ${mapVal["groupName"]}")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "handleDevicesSnapshot - parsing error: $e")
                }
            } else {
                Log.e(TAG, "handleDevicesSnapshot - child ${deviceSnapshot.key} was not a map!")
            }
        }

        return newMap
    }

    /* Constants and Interfaces */
    private const val TAG = "Database"

    interface DeviceListCallback {
        fun onDeviceListChanged(newList: Map<String, List<Device>>)
        fun onDeviceListError(e: Exception)
    }
}



