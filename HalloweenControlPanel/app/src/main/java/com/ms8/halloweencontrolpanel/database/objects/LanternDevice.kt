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

    override fun getVariableDescription(context: Context, id: Int): String {
        return when (id) {
            R.id.btnDropDelayHelp -> context.getString(R.string.lanternDropDelayDesc)
            R.id.btnDropValueHelp -> context.getString(R.string.lanternDropValueDesc)
            R.id.btnRampDelayHelp -> context.getString(R.string.lanternFlickerRateDesc)
            R.id.btnMinBrightnessHelp -> context.getString(R.string.lanternMinBrightnessDesc)
            R.id.btnMaxBrightnessHelp -> context.getString(R.string.lanternMaxBrightnessDesc)
            R.id.btnPinHelp -> context.getString(R.string.lanternPinDesc)
            R.id.btnNameHelp -> context.getString(R.string.lanternNameDesc)
            R.id.btnSmoothingHelp -> context.getString(R.string.lanternSmoothingDesc)
            R.id.btnFlickerDelayMinHelp -> context.getString(R.string.lanternFlickerDelayMinDesc)
            R.id.btnFlickerDelayMaxHelp -> context.getString(R.string.lanternFlickerDelayMaxDesc)
            else -> super.getVariableDescription(context, id)
        }
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