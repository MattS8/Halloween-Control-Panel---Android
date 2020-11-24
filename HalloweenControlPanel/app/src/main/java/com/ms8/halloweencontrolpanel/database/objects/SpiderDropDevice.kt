package com.ms8.halloweencontrolpanel.database.objects

import android.content.Context
import android.content.LocusId
import com.ms8.halloweencontrolpanel.R

class SpiderDropDevice (
        name: String,
        uid: String,
        pin: Int = 1,
        var hangTime: Int = 2000,
        var spiderState: STATE = STATE.RETRACTED,
        var stayDropped: Boolean = false,
        var dropMotorPin: Int = 5,
        var currentSensePin: Int = PIN_A0,
        var retractMotorPin: Int = 4,
        var dropStopSwitchPin: Int = 12,
        var currentPulseDelay: Int = 250,
        var currentLimit:Int = 200
) : Device(name, GroupName, uid, pin) {

    override fun getFirebaseObject(): MutableMap<String, Any?> {
        val returnMap = super.getFirebaseObject()

        returnMap[HANG_TIME] = hangTime
        returnMap[SPIDER_STATE] = spiderState.name /* The app should not directly control the state of the device! */
        returnMap[STAY_DROPPED] = stayDropped
        returnMap[DROP_MOTOR_PIN] = dropMotorPin
        returnMap[CURRENT_SENSE_PIN] = currentSensePin
        returnMap[RETRACT_MOTOR_PIN] = retractMotorPin
        returnMap[DROP_STOP_SWITCH_PIN] = dropStopSwitchPin
        returnMap[CURRENT_PULSE_DELAY] = currentPulseDelay
        returnMap[CURRENT_LIMIT] = currentLimit

        return returnMap
    }

    override fun getVariableDescription(context: Context, id: Int): String {
        return when (id) {
            R.id.btnNameHelp -> context.getString(R.string.spiderDropNameDec)
            R.id.btnPinHelp -> context.getString(R.string.spiderDropPinDesc)
            R.id.btnHangTimeHelp -> context.getString(R.string.spiderDropHangTimeDesc)
            R.id.btnStayDroppedHelp -> context.getString(R.string.spiderDropStayDroppedDesc)
            R.id.btnDropMotorPinHelp -> context.getString(R.string.spiderDropMotorPinDesc)
            R.id.btnCurrentSensePinHelp -> context.getString(R.string.spiderDropCurrentPinDesc)
            R.id.btnRetractMotorPinHelp -> context.getString(R.string.spiderDropRetractPinDesc)
            R.id.btnDropStopSwitchPinHelp -> context.getString(R.string.spiderDropStopSwitchPinDesc)
            R.id.btnCurrentPulseDelayHelp -> context.getString(R.string.spiderDropCurrentPulseDelayDesc)
            R.id.btnCurrentLimitHelp -> context.getString(R.string.spiderDropCurrentLimitDesc)

            else -> super.getVariableDescription(context, id)
        }
    }

    companion object {
        const val GroupName = "Spider Droppers"

        const val HANG_TIME = "hangTime"
        const val SPIDER_STATE = "spiderState"
        const val STAY_DROPPED = "stayDropped"
        const val DROP_MOTOR_PIN = "dropMotorPin"
        const val CURRENT_SENSE_PIN = "currentSensePin"
        const val RETRACT_MOTOR_PIN = "retractMotorPin"
        const val DROP_STOP_SWITCH_PIN = "dropStopSwitchPin"
        const val CURRENT_PULSE_DELAY = "currentPulseDelay"
        const val CURRENT_LIMIT = "currentLimit"

        enum class STATE {DROPPED, RETRACTING, RETRACTED}
    }
}