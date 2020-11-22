package com.ms8.halloweencontrolpanel.database.objects

import android.content.Context
import android.util.Log
import com.ms8.halloweencontrolpanel.R

class LanternDevice(
        name: String,
        uid: String,
        pin: Int = -1,
        var minBrightness: Int = -1,
        var maxBrightness: Int = -1,
        var smoothing: Int = -1,
        var rampDelay: Int = -1,
        var dropDelay: Int = -1,
        var dropValue: Int = -1,
        var flickerDelayMin: Int = -1,
        var flickerDelayMax: Int = -1
): Device(name, GroupName, uid, pin) {

    override fun getFirebaseObject(): MutableMap<String, Any?> {
        val returnMap = super.getFirebaseObject()

        returnMap[PIN] = pin
        returnMap[MIN_BRIGHTNESS] = minBrightness
        returnMap[MAX_BRIGHTNESS] = maxBrightness
        returnMap[SMOOTHING] = smoothing
        returnMap[RAMP_DELAY] = rampDelay
        returnMap[DROP_DELAY] = dropDelay
        returnMap[DROP_VALUE] = dropValue
        returnMap[FLICKER_DELAY_MIN] = flickerDelayMin
        returnMap[FLICKER_DELAY_MAX] = flickerDelayMax

        return returnMap
    }

    override fun getVariableDescription(context: Context, variableName: String): String {
        return when (variableName) {
            NAME -> context.getString(R.string.lanternNameDesc)
            PIN -> context.getString(R.string.lanternPinDesc)
            MIN_BRIGHTNESS -> context.getString(R.string.lanternMinBrightnessDesc)
            MAX_BRIGHTNESS -> context.getString(R.string.lanternMaxBrightnessDesc)
            SMOOTHING -> context.getString(R.string.lanternSmoothingDesc)
            RAMP_DELAY -> context.getString(R.string.lanternFlickerRateDesc)
            DROP_DELAY -> context.getString(R.string.lanternDropDelayDesc)
            DROP_VALUE -> context.getString(R.string.lanternDropValueDesc)
            FLICKER_DELAY_MAX -> context.getString(R.string.lanternFlickerDelayMaxDesc)
            FLICKER_DELAY_MIN -> context.getString(R.string.lanternFlickerDelayMinDesc)
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
        return "\t$NAME\n\t$PIN\n\t$MAX_BRIGHTNESS\n\t$MIN_BRIGHTNESS\n\t$SMOOTHING\n\t$RAMP_DELAY\n\t$DROP_DELAY\n\t$DROP_VALUE"
    }

    companion object {
        const val MIN_BRIGHTNESS = "minBrightness"
        const val MAX_BRIGHTNESS = "maxBrightness"
        const val SMOOTHING = "smoothing"
        const val RAMP_DELAY = "rampDelay"
        const val DROP_DELAY = "dropDelay"
        const val DROP_VALUE = "dropValue"
        const val FLICKER_DELAY_MAX = "flickerDelayMax"
        const val FLICKER_DELAY_MIN = "flickerDelayMin"

        const val GroupName = "Lanterns"
    }
}