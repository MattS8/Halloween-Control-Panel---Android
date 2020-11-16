package com.ms8.halloweencontrolpanel.database.objects

import android.annotation.SuppressLint
import android.content.Context
import android.util.ArrayMap
import android.util.Log
import com.ms8.halloweencontrolpanel.R
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap
import kotlin.math.PI

class LanternDevice(
    name: String,
    uid: String,
    pin: Int = -1,
    var minBrightness: Int = -1,
    var maxBrightness: Int = -1,
    var smoothing: Int = -1,
    var flickerRate: Int = -1,
    var dropDelay: Int = -1,
    var dropValue: Int = -1
): Device(name, GroupName, uid, pin) {

    override fun getFirebaseObject(): MutableMap<String, Any?> {
        val returnMap = super.getFirebaseObject()

        returnMap[PIN] = pin
        returnMap[MIN_BRIGHTNESS] = minBrightness
        returnMap[MAX_BRIGHTNESS] = maxBrightness
        returnMap[SMOOTHING] = smoothing
        returnMap[FLICKER_RATE] = flickerRate
        returnMap[DROP_DELAY] = dropDelay
        returnMap[DROP_VALUE] = dropValue

        return returnMap
    }

    override fun getVariableDescription(context: Context, variableName: String): String {
        return when (variableName) {
            NAME -> context.getString(R.string.lanternNameDesc)
            PIN -> context.getString(R.string.lanternPinDesc)
            MIN_BRIGHTNESS -> context.getString(R.string.lanternMinBrightnessDesc)
            MAX_BRIGHTNESS -> context.getString(R.string.lanternMaxBrightnessDesc)
            SMOOTHING -> context.getString(R.string.lanternSmoothingDesc)
            FLICKER_RATE -> context.getString(R.string.lanternFlickerRateDesc)
            DROP_DELAY -> context.getString(R.string.lanternDropDelayDesc)
            DROP_VALUE -> context.getString(R.string.lanternDropValueDesc)
            else -> {
                Log.e("LanternDevice",
                    "getVariableDescription - Unknown variable name: $variableName. Should be one of:\n " +
                            getVariableNames()
                )
                ""
            }
        }
    }

    private fun getVariableNames() : String {
        return "\t$NAME\n\t$PIN\n\t$MAX_BRIGHTNESS\n\t$MIN_BRIGHTNESS\n\t$SMOOTHING\n\t$FLICKER_RATE\n\t$DROP_DELAY\n\t$DROP_VALUE"
    }

    companion object {
        const val MIN_BRIGHTNESS = "minBrightness"
        const val MAX_BRIGHTNESS = "maxBrightness"
        const val SMOOTHING = "smoothing"
        const val FLICKER_RATE = "flickerRate"
        const val DROP_DELAY = "dropDelay"
        const val DROP_VALUE = "dropValue"

        const val GroupName = "Lanterns"
    }
}