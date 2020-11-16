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
import kotlin.Exception

object FirebaseDao {
    val devices: ObservableField<MutableMap<String, MutableList<Device>>> = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
        ObservableField(ArrayMap())
    } else {
        ObservableField(HashMap())
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
                        LanternDevice.GroupName -> {
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
                                        (mapVal[LanternDevice.FLICKER_RATE] as Number).toInt(),
                                        (mapVal[LanternDevice.DROP_DELAY] as Number).toInt(),
                                        (mapVal[LanternDevice.DROP_VALUE] as Number).toInt()
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

    const val TAG = "Database"

    interface DeviceListCallback {
        fun onDeviceListChanged(newList: Map<String, List<Device>>)
        fun onDeviceListError(e: Exception)
    }
}



