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
        var stayDropped: Boolean = false
) : Device(name, GroupName, uid, pin){

    override fun getFirebaseObject(): MutableMap<String, Any?> {
        val returnMap = super.getFirebaseObject()

        returnMap[HANG_TIME] = hangTime
        returnMap[SPIDER_STATE] = spiderState
        returnMap[STAY_DROPPED] = stayDropped

        return returnMap
    }

    override fun getVariableDescription(context: Context, id: Int): String {
        return when (id) {
            R.id.btnNameHelp -> context.getString(R.string.spiderDropNameDec)
            R.id.btnPinHelp -> context.getString(R.string.spiderDropPinDesc)
            R.id.btnHangTimeHelp -> context.getString(R.string.spiderDropHangTimeDesc)
            R.id.btnStayDroppedHelp -> context.getString(R.string.spiderDropStayDroppedDesc)

            else -> super.getVariableDescription(context, id)
        }
    }

    companion object {
        const val GroupName = "Spider Droppers"

        const val HANG_TIME = "hangTime"
        const val SPIDER_STATE = "spiderState"
        const val STAY_DROPPED = "stayDropped"

        enum class STATE {DROPPED, RETRACTING, RETRACTED}
    }
}