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
import com.ms8.halloweencontrolpanel.database.objects.SpiderDropDevice.Companion.CURRENT_LIMIT
import com.ms8.halloweencontrolpanel.database.objects.SpiderDropDevice.Companion.CURRENT_PULSE_DELAY
import com.ms8.halloweencontrolpanel.database.objects.SpiderDropDevice.Companion.CURRENT_SENSE_PIN
import com.ms8.halloweencontrolpanel.database.objects.SpiderDropDevice.Companion.DROP_MOTOR_DELAY
import com.ms8.halloweencontrolpanel.database.objects.SpiderDropDevice.Companion.DROP_MOTOR_PIN
import com.ms8.halloweencontrolpanel.database.objects.SpiderDropDevice.Companion.DROP_STOP_SWITCH_PIN
import com.ms8.halloweencontrolpanel.database.objects.SpiderDropDevice.Companion.HANG_TIME
import com.ms8.halloweencontrolpanel.database.objects.SpiderDropDevice.Companion.RETRACT_MOTOR_PIN
import com.ms8.halloweencontrolpanel.database.objects.SpiderDropDevice.Companion.SPIDER_STATE
import com.ms8.halloweencontrolpanel.database.objects.SpiderDropDevice.Companion.STAY_DROPPED
import com.ms8.halloweencontrolpanel.database.objects.SpiderDropDevice.Companion.UP_LIMIT_SWITCH_PIN
import kotlin.Exception

object FirebaseDao {
    val devices: ObservableField<MutableMap<String, MutableList<Device>>> =
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            ObservableField(ArrayMap())
        } else {
            ObservableField(HashMap())
        }

    /* Public Accessors */
    fun getDevice(uid: String, groupName: String? = null): Device? {
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

    fun listenToState(deviceUID: String, listener: ValueEventListener) {
        FirebaseDatabase.getInstance().getReference("state").child(deviceUID)
            .addValueEventListener(listener)
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
        FirebaseDatabase.getInstance().getReference("devices").child(deviceUID).child("command")
            .setValue("_none_").addOnSuccessListener {
                FirebaseDatabase.getInstance().getReference("devices").child(deviceUID)
                    .child("command")
                    .setValue(command)
            }
    }

    private fun parseLanternDevice(snapshotMap: Map<*, *>, uid: String): LanternDevice {
        val name = if (snapshotMap["name"] is String)
            snapshotMap["name"] as String else {
            Log.e(TAG, "parseLanternDevice - Unable to parse name (read: ${snapshotMap["name"]})")
            uid}
        val pin = if (snapshotMap[Device.PIN] is Number)
            (snapshotMap[Device.PIN] as Number).toInt() else {
            Log.e(TAG, "parseLanternDevice - Unable to parse pin (read: ${snapshotMap[Device.PIN]})")
            -1}
        val minBrightness = if (snapshotMap[LanternDevice.MIN_BRIGHTNESS] is Number)
            (snapshotMap[LanternDevice.MIN_BRIGHTNESS] as Number).toInt() else {
            Log.e(TAG, "parseLanternDevice - Unable to parse minBrightness (read: ${snapshotMap[LanternDevice.MIN_BRIGHTNESS]})")
            0}
        val maxBrightness = if (snapshotMap[LanternDevice.MAX_BRIGHTNESS] is Number)
            (snapshotMap[LanternDevice.MAX_BRIGHTNESS] as Number).toInt() else {
            Log.e(TAG, "parseLanternDevice - Unable to parse maxBrightness (read: ${snapshotMap[LanternDevice.MAX_BRIGHTNESS]})")
            0}
        val smoothing = if (snapshotMap[LanternDevice.SMOOTHING] is Number)
            (snapshotMap[LanternDevice.SMOOTHING] as Number).toInt() else {
            Log.e(TAG, "parseLanternDevice - Unable to parse smoothing (read: ${snapshotMap[LanternDevice.SMOOTHING]})")
            0}
        val rampDelay = if (snapshotMap[LanternDevice.RAMP_DELAY] is Number)
            (snapshotMap[LanternDevice.RAMP_DELAY] as Number).toInt() else {
            Log.e(TAG, "parseLanternDevice - Unable to parse rampDelay (read: ${snapshotMap[LanternDevice.RAMP_DELAY]})")
            0}
        val dropDelay = if (snapshotMap[LanternDevice.DROP_DELAY] is Number)
            (snapshotMap[LanternDevice.DROP_DELAY] as Number).toInt() else {
            Log.e(TAG, "parseLanternDevice - Unable to parse dropDelay (read: ${snapshotMap[LanternDevice.DROP_DELAY]})")
            0}
        val dropValue = if (snapshotMap[LanternDevice.DROP_VALUE] is Number)
            (snapshotMap[LanternDevice.DROP_VALUE] as Number).toInt() else {
            Log.e(TAG, "parseLanternDevice - Unable to parse dropValue (read: ${snapshotMap[LanternDevice.DROP_VALUE]})")
            0}
        val flickerDelay = if (snapshotMap[LanternDevice.FLICKER_DELAY_MIN] is Number)
            (snapshotMap[LanternDevice.FLICKER_DELAY_MIN] as Number).toInt() else {
            Log.e(TAG, "parseLanternDevice - Unable to parse flickerDelay (read: ${snapshotMap[LanternDevice.FLICKER_DELAY_MIN]})")
            0}
        val flickerDelayMax = if (snapshotMap[LanternDevice.FLICKER_DELAY_MAX] is Number)
            (snapshotMap[LanternDevice.FLICKER_DELAY_MAX] as Number).toInt() else {
            Log.e(TAG, "parseLanternDevice - Unable to parse flickerDelayMax (read: ${snapshotMap[LanternDevice.FLICKER_DELAY_MAX]})")
            0}

        return LanternDevice(name, uid, pin, minBrightness, maxBrightness, smoothing, rampDelay, dropDelay,
            dropValue, flickerDelay, flickerDelayMax)
    }

    private fun parseSpiderDropDevice(snapshotMap: Map<*, *>, uid: String): SpiderDropDevice {
        val name = if (snapshotMap["name"] is String)
            snapshotMap["name"] as String else uid
        val pin = if (snapshotMap[Device.PIN] is Number)
            (snapshotMap[Device.PIN] as Number).toInt()
            else {
                Log.e(TAG, "parseSpiderDropDevice - Unable to parse pin (val read: ${snapshotMap[Device.PIN]})")
                -1}
        val hangTime = if (snapshotMap[HANG_TIME] is Number)
            (snapshotMap[HANG_TIME] as Number).toInt()
            else {
                Log.e(TAG, "parseSpiderDropDevice - Unable to parse hangTime (val read: ${snapshotMap[HANG_TIME]})")
                0}
        val stayDropped = if (snapshotMap[STAY_DROPPED] is Boolean)
            (snapshotMap[STAY_DROPPED] as Boolean)
            else {
                Log.e(TAG, "parseSpiderDropDevice - Unable to parse stayDropped (val read: ${snapshotMap[STAY_DROPPED]})")
                false}
        val dropMotorPin = if (snapshotMap[DROP_MOTOR_PIN] is Number)
            (snapshotMap[DROP_MOTOR_PIN] as Number).toInt()
            else {
                Log.e(TAG, "parseSpiderDropDevice - Unable to parse dropMotorPin (val read: ${snapshotMap[DROP_MOTOR_PIN]})")
                -1}
        val currentSensePin = if (snapshotMap[CURRENT_SENSE_PIN] is Number)
            (snapshotMap[CURRENT_SENSE_PIN] as Number).toInt()
            else {
                Log.e(TAG, "parseSpiderDropDevice - Unable to parse currentSensePin (val read: ${snapshotMap[CURRENT_SENSE_PIN]})")
                -1}
        val retractMotorPin = if (snapshotMap[RETRACT_MOTOR_PIN] is Number)
            (snapshotMap[RETRACT_MOTOR_PIN] as Number).toInt()
            else {
                Log.e(TAG, "parseSpiderDropDevice - Unable to parse retractMotorPin (val read: ${snapshotMap[RETRACT_MOTOR_PIN]})")
                0}
        val dropStopSwitchPin = if (snapshotMap[DROP_STOP_SWITCH_PIN] is Number)
            (snapshotMap[DROP_STOP_SWITCH_PIN] as Number).toInt()
            else {
                Log.e(TAG, "parseSpiderDropDevice - Unable to parse dropStopSwitchPin (val read: ${snapshotMap[DROP_STOP_SWITCH_PIN]})")
                -1}
        val currentPulseDelay = if (snapshotMap[CURRENT_PULSE_DELAY] is Number)
            (snapshotMap[CURRENT_PULSE_DELAY] as Number).toInt()
            else {
                Log.e(TAG, "parseSpiderDropDevice - Unable to parse currentPulseDelay (val read: ${snapshotMap[CURRENT_PULSE_DELAY]})")
                0}
        val currentLimit = if (snapshotMap[CURRENT_LIMIT] is Number)
            (snapshotMap[CURRENT_LIMIT] as Number).toInt()
            else {
                Log.e(TAG, "parseSpiderDropDevice - Unable to parse currentLimit (val read: ${snapshotMap[CURRENT_LIMIT]})")
                0}
        val dropMotorDelay = if (snapshotMap[DROP_MOTOR_DELAY] is Number)
            (snapshotMap[DROP_MOTOR_DELAY] as Number).toInt()
            else {
                Log.e(TAG, "parseSpiderDropDevice - Unable to parse dropMotorDelay (val read: ${snapshotMap[DROP_MOTOR_DELAY]})")
                0}
        val upLimitSwitchPin = if (snapshotMap[UP_LIMIT_SWITCH_PIN] is Number)
            (snapshotMap[UP_LIMIT_SWITCH_PIN] as Number).toInt()
            else {
                Log.e(TAG, "parseSpiderDropDevice - Unable to parse upLimitSwitchPin (val read: ${snapshotMap[UP_LIMIT_SWITCH_PIN]})")
                -1}

        return SpiderDropDevice(name, uid, pin, hangTime, SpiderDropDevice.Companion.STATE.RETRACTED,
            stayDropped, dropMotorPin, currentSensePin, retractMotorPin, dropStopSwitchPin,
            currentPulseDelay, currentLimit, dropMotorDelay, upLimitSwitchPin)
    }

    /* Helper Functions */
    private fun parseDeviceListSnapshot(snapshot: DataSnapshot): MutableMap<String, MutableList<Device>> {
        val newMap: MutableMap<String, MutableList<Device>> =
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT)
                ArrayMap()
            else
                HashMap()

        snapshot.children.forEach { deviceSnapshot ->
            Log.d(TAG, "handleDevicesSnapshot - ${deviceSnapshot.key}")
            if (deviceSnapshot.value is Map<*, *>) {
                try {
                    val mapVal = deviceSnapshot.value as Map<*, *>
                    when (mapVal["groupName"]) {
                        LanternDevice.GroupName -> {
                            val uid = deviceSnapshot.key ?: throw Exception("Invalid uid")

                            if (!newMap.containsKey(LanternDevice.GroupName))
                                newMap[LanternDevice.GroupName] = ArrayList()

                            newMap[LanternDevice.GroupName]?.add(parseLanternDevice(mapVal, uid))
                        }

                        SpiderDropDevice.GroupName -> {
                            val uid = deviceSnapshot.key ?: throw Exception("Invalid uid")

                            if (!newMap.containsKey(SpiderDropDevice.GroupName))
                                newMap[SpiderDropDevice.GroupName] = ArrayList()

                            newMap[SpiderDropDevice.GroupName]?.add(parseSpiderDropDevice(mapVal, uid))

                            FirebaseDatabase.getInstance().getReference("state").child(uid)
                                .addValueEventListener(object : ValueEventListener {
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        if (snapshot.value is String) {
                                            newMap[SpiderDropDevice.GroupName]?.forEach { device ->
                                                if (device.uid == uid) {
                                                    (device as SpiderDropDevice).spiderState =
                                                        SpiderDropDevice.Companion.STATE.valueOf(
                                                            snapshot.value as String
                                                        )
                                                    devices.notifyChange()
                                                }
                                            }
                                        } else { Log.e(TAG, "State wasn't a string") }
                                    }

                                    override fun onCancelled(error: DatabaseError) {}
                                })
                            Log.d(TAG, "dropPin = ${(mapVal[Device.PIN] as Number).toInt()}")
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



