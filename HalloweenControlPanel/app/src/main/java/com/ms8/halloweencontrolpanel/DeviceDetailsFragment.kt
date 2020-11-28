package com.ms8.halloweencontrolpanel

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.ms8.halloweencontrolpanel.database.FirebaseDao
import com.ms8.halloweencontrolpanel.database.objects.Device
import com.ms8.halloweencontrolpanel.database.objects.Device.Companion.PIN_A0
import com.ms8.halloweencontrolpanel.database.objects.LanternDevice
import com.ms8.halloweencontrolpanel.database.objects.SpiderDropDevice
import com.ms8.halloweencontrolpanel.databinding.ContentLanternControlsBinding
import com.ms8.halloweencontrolpanel.databinding.ContentLanternControlsV2Binding
import com.ms8.halloweencontrolpanel.databinding.ContentSpiderDropControlsBinding
import com.ms8.halloweencontrolpanel.databinding.ContentSpiderDropControlsV2Binding
import kotlinx.android.synthetic.main.content_spider_drop_controls.*

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class DeviceDetailsFragment : Fragment() {
    private var spiderBtn: ExtendedFloatingActionButton? = null
    private var spiderStateTextView: TextView? = null

    private val spiderStateListener: ValueEventListener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            if (snapshot.value is String) {
                val newSate = SpiderDropDevice.Companion.STATE.valueOf(snapshot.value as String)
                (device as SpiderDropDevice).spiderState = newSate
                setupSpiderBtnDrop()
                setupSpiderStateTv()
            }
        }

        override fun onCancelled(error: DatabaseError) {}
    }

    var mode = PRECISION
    var showingPins = false
    lateinit var device: Device

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("detailMode", mode)
        outState.putBoolean("showingPins", showingPins)
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        mode = savedInstanceState?.getInt("detailMode") ?: mode
        showingPins = savedInstanceState?.getBoolean("showingPins") ?: showingPins

        val rootView = inflater.inflate(R.layout.fragment_device_controls, container, false)
        device = FirebaseDao.getDevice(arguments?.getString("DeviceUID") ?: throw Exception("Missing device UID argument!"))
            ?: throw Exception("Unable to retrieve details for device ${arguments?.getString("DeviceUID")}")

        when (device.groupName) {
            LanternDevice.GroupName -> setupLanternLayout(rootView, inflater)
            SpiderDropDevice.GroupName -> setupSpiderLayout(rootView, inflater)
                    .also { FirebaseDao.listenToState(device.uid, spiderStateListener) }
            else ->
                throw Exception("Unknown device type ${arguments?.get("DeviceType")}. Can't handle fragment inner view!")
        }

        return rootView
    }

    override fun onDestroyView() {
        super.onDestroyView()

        when (device.groupName) {
            SpiderDropDevice.GroupName -> FirebaseDao.stopListeningToValue(SpiderDropDevice.SPIDER_STATE, device.uid, spiderStateListener)
        }
    }

    private fun setupSpiderLayout(rootView: View, inflater: LayoutInflater) {
        when (mode) {
            PRECISION -> setupSpiderDetailsWithEditTexts(rootView, inflater)
            SLIDER -> setupSpiderDetailsWithSliders(rootView, inflater)
        }
    }

    private fun setupSpiderStateTv() {
        context?.let { c ->
            (device as SpiderDropDevice).let { sDevice ->
                spiderStateTextView?.let { tvStateVal ->
                    tvStateVal.text = sDevice.spiderState.name
                    tvStateVal.setTextColor(
                            when (sDevice.spiderState) {
                                SpiderDropDevice.Companion.STATE.DROPPED -> ContextCompat.getColor(c, R.color.spiderDropTextColor)
                                SpiderDropDevice.Companion.STATE.RETRACTING -> ContextCompat.getColor(c, R.color.spiderRetractingTextColor)
                                SpiderDropDevice.Companion.STATE.RETRACTED -> ContextCompat.getColor(c, R.color.spiderRetractedTextColor)
                            }
                    )
                }
            }
        }
    }

    private fun setupSpiderBtnDrop() {
        context?.let { c ->
            (device as SpiderDropDevice).let { sDevice->
                spiderBtn?.let {btnDrop ->
                    btnDrop.backgroundTintList = when (sDevice.spiderState) {
                        SpiderDropDevice.Companion.STATE.DROPPED -> ContextCompat.getColorStateList(c, R.color.spiderRetractColor)
                        SpiderDropDevice.Companion.STATE.RETRACTING -> ContextCompat.getColorStateList(c, R.color.spiderRetractingColor)
                        SpiderDropDevice.Companion.STATE.RETRACTED -> ContextCompat.getColorStateList(c, R.color.spiderDropColor)
                    }
                    btnDrop.text = when (sDevice.spiderState) {
                        SpiderDropDevice.Companion.STATE.DROPPED -> c.getString(R.string.retract_spider)
                        SpiderDropDevice.Companion.STATE.RETRACTING -> c.getString(R.string.retracting_btn_text)
                        SpiderDropDevice.Companion.STATE.RETRACTED -> c.getString(R.string.drop_spider)
                    }
                }
            }
        }
    }

    private fun setupSpiderDetailsWithSliders(rootView: View, inflater: LayoutInflater) {
        val frameLayout = rootView.findViewById<FrameLayout>(R.id.detailsContainer)
        val spiderControlsBinding = ContentSpiderDropControlsV2Binding.inflate(inflater)
        context?.let { c ->
            spiderControlsBinding.apply {
                (device as SpiderDropDevice).let { sDevice ->
                    // Back Button
                    btnBack.setOnClickListener { goBack() }

                    // Name
                    etName.setText(sDevice.name)

                    // Drop Pin
                    ArrayAdapter.createFromResource(
                            c,
                            R.array.pin_values_all,
                            android.R.layout.simple_spinner_item
                    ).also { adapter ->
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        spnDropPin.adapter = adapter
                    }
                    spnDropPin.setSelection(posFromPin(sDevice.pin))

                    // Drop Motor Pin
                    ArrayAdapter.createFromResource(
                            c,
                            R.array.pin_values_all,
                            android.R.layout.simple_spinner_item
                    ).also { adapter ->
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        spnDropMotorPin.adapter = adapter
                    }
                    spnDropMotorPin.setSelection(posFromPin(sDevice.dropMotorPin))

                    // Current Sense Pin
                    ArrayAdapter.createFromResource(
                            c,
                            R.array.pin_values_all,
                            android.R.layout.simple_spinner_item
                    ).also { adapter ->
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        spnCurrentSense.adapter = adapter
                    }
                    spnCurrentSense.setSelection(posFromPin(sDevice.currentSensePin))

                    // Retract Motor Pin
                    ArrayAdapter.createFromResource(
                            c,
                            R.array.pin_values_all,
                            android.R.layout.simple_spinner_item
                    ).also { adapter ->
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        spnRetractMotor.adapter = adapter
                    }
                    spnRetractMotor.setSelection(posFromPin(sDevice.retractMotorPin))

                    // Drop Stop Switch Pin
                    ArrayAdapter.createFromResource(
                            c,
                            R.array.pin_values_all,
                            android.R.layout.simple_spinner_item
                    ).also { adapter ->
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        spnDropStopSwitch.adapter = adapter
                    }
                    spnDropStopSwitch.setSelection(posFromPin(sDevice.dropStopSwitchPin))

                    // Up Limit Switch Pin
                    ArrayAdapter.createFromResource(
                            c,
                            R.array.pin_values_all,
                            android.R.layout.simple_spinner_item
                    ).also { adapter ->
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        spnUpLimitSwitchPin.adapter = adapter
                    }
                    spnUpLimitSwitchPin.setSelection(posFromPin(sDevice.upLimitSwitchPin))

                    // Hang Time
                    val hangTimeStepSize = 100
                    tvHangTimeVal.text = sDevice.hangTime.toString()
                    skHangTime.progress = sDevice.hangTime
                    skHangTime.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
                        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                            if (fromUser)
                                seekBar?.progress = (progress/hangTimeStepSize)*hangTimeStepSize
                            tvHangTimeVal.text = seekBar?.progress?.toString()
                        }

                        override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                        override fun onStopTrackingTouch(seekBar: SeekBar?) {}
                    })
                    tvHangTimeVal.isEnabled = !sDevice.stayDropped
                    skHangTime.isEnabled = !sDevice.stayDropped
                    tvHangTimeLabel.isEnabled = !sDevice.stayDropped

                    // Current Pulse Delay
                    tvCurrentPulseDelayVal.text = sDevice.currentPulseDelay.toString()
                    skCurrentPulseDelay.progress = sDevice.currentPulseDelay
                    skCurrentPulseDelay.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
                        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                            tvCurrentPulseDelayVal.text = progress.toString()
                        }

                        override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                        override fun onStopTrackingTouch(seekBar: SeekBar?) {}
                    })

                    // Current Limit
                    tvCurrentLimitVal.text = sDevice.currentLimit.toString()
                    skCurrentLimit.progress = sDevice.currentPulseDelay
                    skCurrentLimit.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
                        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                            tvCurrentLimitVal.text = progress.toString()
                        }

                        override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                        override fun onStopTrackingTouch(seekBar: SeekBar?) {}
                    })

                    // Drop Motor Delay
                    val dropMotorDelayStepSize = 100
                    tvDropMotorDelayVal.text = sDevice.dropMotorDelay.toString()
                    skDropMotorDelay.progress = sDevice.currentPulseDelay
                    skDropMotorDelay.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
                        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                            if (fromUser)
                                seekBar?.progress = (progress/dropMotorDelayStepSize)*dropMotorDelayStepSize
                            tvDropMotorDelayVal.text = progress.toString()
                        }

                        override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                        override fun onStopTrackingTouch(seekBar: SeekBar?) {}
                    })

                    // Stay Dropped
                    ckStayDropped.isChecked = sDevice.stayDropped
                    ckStayDropped.setOnCheckedChangeListener { _, isChecked ->
                        skHangTime.isEnabled = !isChecked
                        tvHangTimeLabel.isEnabled = !isChecked
                        tvHangTimeVal.isEnabled = !isChecked
                    }

                    // Show Pins Btn
                    btnShowPins.text = if (showingPins) getString(R.string.hide_pins) else getString(R.string.show_pins)
                    btnShowPins.setOnClickListener {
                        showingPins = !showingPins
                        (it as Button).apply {
                            btnShowPins.text = if (showingPins) getString(R.string.hide_pins) else getString(R.string.show_pins)
                            pinsLayout.visibility = if (showingPins) View.VISIBLE else View.GONE
                        }
                    }
                    pinsLayout.visibility = if (showingPins) View.VISIBLE else View.GONE

                    // Help Buttons
                    btnHangTimeHelp.setOnClickListener { onHelpButtonClicked(R.id.btnHangTimeHelp, R.string.hanging_time_ms) }
                    btnNameHelp.setOnClickListener { onHelpButtonClicked(R.id.btnNameHelp, R.string.name) }
                    btnStayDroppedHelp.setOnClickListener { onHelpButtonClicked(R.id.btnStayDroppedHelp, R.string.stay_dropped) }
                    btnDropPinHelp.setOnClickListener { onHelpButtonClicked(R.id.btnDropPinHelp, R.string.drop_pin_label) }
                    btnDropMotorPinHelp.setOnClickListener { onHelpButtonClicked(R.id.btnDropMotorPinHelp, R.string.drop_motor_pin_label) }
                    btnCurrentSensePinHelp.setOnClickListener { onHelpButtonClicked(R.id.btnCurrentSensePinHelp, R.string.current_sense_pin_label) }
                    btnRetractMotorPinHelp.setOnClickListener { onHelpButtonClicked(R.id.btnRetractMotorPinHelp, R.string.retract_motor_pin_label) }
                    btnDropStopSwitchPinHelp.setOnClickListener { onHelpButtonClicked(R.id.btnDropStopSwitchPinHelp, R.string.drop_stop_switch_pin_label) }
                    btnCurrentPulseDelayHelp.setOnClickListener { onHelpButtonClicked(R.id.btnCurrentPulseDelayHelp, R.string.current_pulse_delay_label) }
                    btnCurrentLimitHelp.setOnClickListener { onHelpButtonClicked(R.id.btnCurrentLimitHelp, R.string.current_limit_label) }
                    btnDropMotorDelayHelp.setOnClickListener { onHelpButtonClicked(R.id.btnDropMotorDelayHelp, R.string.drop_motor_delay_label) }
                    btnUpLimitSwitchPinHelp.setOnClickListener { onHelpButtonClicked(R.id.btnUpLimitSwitchPinHelp, R.string.up_limit_switch_pin_label) }

                    // State
                    spiderStateTextView = tvStateVal
                    setupSpiderStateTv()

                    // Spider Action Button
                    spiderBtn = btnDrop
                    setupSpiderBtnDrop()
                    btnDrop.setOnClickListener {
                        when (sDevice.spiderState) {
                            SpiderDropDevice.Companion.STATE.DROPPED -> FirebaseDao.sendCommand("RETRACT", device.uid)
                            SpiderDropDevice.Companion.STATE.RETRACTING -> Snackbar.make(rootView, R.string.err_spider_retracting, Snackbar.LENGTH_LONG)
                                    .setAction(R.string.drop_anyway) { FirebaseDao.sendCommand("DROP", device.uid) }
                                    .setActionTextColor(ContextCompat.getColor(c, android.R.color.holo_red_dark))
                                    .show()
                            SpiderDropDevice.Companion.STATE.RETRACTED -> FirebaseDao.sendCommand("DROP", device.uid)
                        }
                    }

                    // Update Button
                    btnUpdate.setOnClickListener {
                        sDevice.name = etName.text.toString()
                        sDevice.hangTime = skHangTime.progress
                        sDevice.stayDropped = ckStayDropped.isChecked
                        sDevice.pin = pinFromPos(spnDropPin.selectedItemPosition)
                        sDevice.dropMotorPin = pinFromPos(spnDropMotorPin.selectedItemPosition)
                        sDevice.currentSensePin = pinFromPos(spnCurrentSense.selectedItemPosition)
                        sDevice.retractMotorPin = pinFromPos(spnRetractMotor.selectedItemPosition)
                        sDevice.dropStopSwitchPin = pinFromPos(spnDropStopSwitch.selectedItemPosition)
                        sDevice.currentPulseDelay = skCurrentPulseDelay.progress
                        sDevice.currentLimit = skCurrentLimit.progress
                        sDevice.dropMotorDelay = skDropMotorDelay.progress
                        sDevice.upLimitSwitchPin = pinFromPos(spnUpLimitSwitchPin.selectedItemPosition)

                        FirebaseDao.updateDevice(sDevice)
                    }

                    // Change Layout Type Button
                    btnChangeToPreciseMode.setOnClickListener {
                        mode = PRECISION
                        frameLayout.removeAllViews()
                        setupSpiderDetailsWithEditTexts(rootView, inflater)
                    }
                }
            }
        }

        frameLayout.addView(spiderControlsBinding.spiderDropControlsContent)
    }

    private fun setupSpiderDetailsWithEditTexts(rootView: View, inflater: LayoutInflater) {
        val frameLayout = rootView.findViewById<FrameLayout>(R.id.detailsContainer)
        val spiderControlsBinding = ContentSpiderDropControlsBinding.inflate(inflater)
        context?.let { c ->
            spiderControlsBinding.apply {
                (device as SpiderDropDevice).let { sDevice ->
                    // Back Button
                    btnBack.setOnClickListener { goBack() }

                    // Name
                    etName.setText(sDevice.name)

                    // Drop Pin
                    ArrayAdapter.createFromResource(
                            c,
                            R.array.pin_values_all,
                            android.R.layout.simple_spinner_item
                    ).also { adapter ->
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        spnDropPin.adapter = adapter
                    }
                    spnDropPin.setSelection(posFromPin(sDevice.pin))

                    // Drop Motor Pin
                    ArrayAdapter.createFromResource(
                            c,
                            R.array.pin_values_all,
                            android.R.layout.simple_spinner_item
                    ).also { adapter ->
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        spnDropMotorPin.adapter = adapter
                    }
                    spnDropMotorPin.setSelection(posFromPin(sDevice.dropMotorPin))

                    // Current Sense Pin
                    ArrayAdapter.createFromResource(
                            c,
                            R.array.pin_values_all,
                            android.R.layout.simple_spinner_item
                    ).also { adapter ->
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        spnCurrentSense.adapter = adapter
                    }
                    spnCurrentSense.setSelection(posFromPin(sDevice.currentSensePin))

                    // Retract Motor Pin
                    ArrayAdapter.createFromResource(
                            c,
                            R.array.pin_values_all,
                            android.R.layout.simple_spinner_item
                    ).also { adapter ->
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        spnRetractMotor.adapter = adapter
                    }
                    spnRetractMotor.setSelection(posFromPin(sDevice.retractMotorPin))

                    // Drop Stop Switch Pin
                    ArrayAdapter.createFromResource(
                            c,
                            R.array.pin_values_all,
                            android.R.layout.simple_spinner_item
                    ).also { adapter ->
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        spnDropStopSwitch.adapter = adapter
                    }
                    spnDropStopSwitch.setSelection(posFromPin(sDevice.dropStopSwitchPin))

                    // Up Limit Switch Pin
                    ArrayAdapter.createFromResource(
                            c,
                            R.array.pin_values_all,
                            android.R.layout.simple_spinner_item
                    ).also { adapter ->
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        spnUpLimitSwitchPin.adapter = adapter
                    }
                    spnUpLimitSwitchPin.setSelection(posFromPin(sDevice.upLimitSwitchPin))

                    // Hang Time
                    etHangTime.setText(sDevice.hangTime.toString())
                    etHangTime.isEnabled = !sDevice.stayDropped
                    tvHangTimeLabel.isEnabled = !sDevice.stayDropped

                    // Current Pulse Delay
                    etCurrentPulseDelay.setText(sDevice.currentPulseDelay.toString())

                    // Current Limit
                    etCurrentLimit.setText(sDevice.currentLimit.toString())

                    // Stay Dropped
                    ckStayDropped.isChecked = sDevice.stayDropped
                    ckStayDropped.setOnCheckedChangeListener { _, isChecked ->
                        tvHangTimeLabel.isEnabled = !isChecked
                        etHangTime.isEnabled = !isChecked
                    }

                    // Drop Motor Delay
                    etDropMotorDelay.setText(sDevice.dropMotorDelay.toString())

                    // Show Pins Btn
                    btnShowPins.text = if (showingPins) getString(R.string.hide_pins) else getString(R.string.show_pins)
                    btnShowPins.setOnClickListener {
                        showingPins = !showingPins
                        (it as Button).apply {
                            btnShowPins.text = if (showingPins) getString(R.string.hide_pins) else getString(R.string.show_pins)
                            pinsLayout.visibility = if (showingPins) View.VISIBLE else View.GONE
                        }
                    }
                    pinsLayout.visibility = if (showingPins) View.VISIBLE else View.GONE

                    // Help Buttons
                    btnHangTimeHelp.setOnClickListener { onHelpButtonClicked(R.id.btnHangTimeHelp, R.string.hanging_time_ms) }
                    btnNameHelp.setOnClickListener { onHelpButtonClicked(R.id.btnNameHelp, R.string.name) }
                    btnStayDroppedHelp.setOnClickListener { onHelpButtonClicked(R.id.btnStayDroppedHelp, R.string.stay_dropped) }
                    btnDropPinHelp.setOnClickListener { onHelpButtonClicked(R.id.btnDropPinHelp, R.string.drop_pin_label) }
                    btnDropMotorPinHelp.setOnClickListener { onHelpButtonClicked(R.id.btnDropMotorPinHelp, R.string.drop_motor_pin_label) }
                    btnCurrentSensePinHelp.setOnClickListener { onHelpButtonClicked(R.id.btnCurrentSensePinHelp, R.string.current_sense_pin_label) }
                    btnRetractMotorPinHelp.setOnClickListener { onHelpButtonClicked(R.id.btnRetractMotorPinHelp, R.string.retract_motor_pin_label) }
                    btnDropStopSwitchPinHelp.setOnClickListener { onHelpButtonClicked(R.id.btnDropStopSwitchPinHelp, R.string.drop_stop_switch_pin_label) }
                    btnCurrentPulseDelayHelp.setOnClickListener { onHelpButtonClicked(R.id.btnCurrentPulseDelayHelp, R.string.current_pulse_delay_label) }
                    btnCurrentLimitHelp.setOnClickListener { onHelpButtonClicked(R.id.btnCurrentLimitHelp, R.string.current_limit_label) }
                    btnDropMotorDelayHelp.setOnClickListener { onHelpButtonClicked(R.id.btnDropMotorDelayHelp, R.string.drop_motor_delay_label) }
                    btnUpLimitSwitchPinHelp.setOnClickListener { onHelpButtonClicked(R.id.btnUpLimitSwitchPinHelp, R.string.up_limit_switch_pin_label) }

                    // State
                    spiderStateTextView = tvStateVal
                    setupSpiderStateTv()

                    // Spider Action Button
                    spiderBtn = btnDrop
                    setupSpiderBtnDrop()
                    btnDrop.setOnClickListener {
                        when (sDevice.spiderState) {
                            SpiderDropDevice.Companion.STATE.DROPPED -> FirebaseDao.sendCommand("RETRACT", device.uid)
                            SpiderDropDevice.Companion.STATE.RETRACTING -> Snackbar.make(rootView, R.string.err_spider_retracting, Snackbar.LENGTH_LONG)
                                    .setAction(R.string.drop_anyway) { FirebaseDao.sendCommand("DROP", device.uid) }
                                    .setActionTextColor(ContextCompat.getColor(c, android.R.color.holo_red_dark))
                                    .show()
                            SpiderDropDevice.Companion.STATE.RETRACTED -> FirebaseDao.sendCommand("DROP", device.uid)
                        }
                    }

                    // Update Button
                    btnUpdate.setOnClickListener {
                        sDevice.name = etName.text.toString()
                        sDevice.hangTime = etHangTime.text.toString().toInt()
                        sDevice.stayDropped = ckStayDropped.isChecked
                        sDevice.pin = pinFromPos(spnDropPin.selectedItemPosition)
                        sDevice.dropMotorPin = pinFromPos(spnDropMotorPin.selectedItemPosition)
                        sDevice.currentSensePin = pinFromPos(spnCurrentSense.selectedItemPosition)
                        sDevice.retractMotorPin = pinFromPos(spnRetractMotor.selectedItemPosition)
                        sDevice.dropStopSwitchPin = pinFromPos(spnDropStopSwitch.selectedItemPosition)
                        sDevice.currentPulseDelay = etCurrentPulseDelay.text.toString().toInt()
                        sDevice.currentLimit = etCurrentLimit.text.toString().toInt()
                        sDevice.dropMotorDelay = etDropMotorDelay.text.toString().toInt()
                        sDevice.upLimitSwitchPin = pinFromPos(spnUpLimitSwitchPin.selectedItemPosition)

                        FirebaseDao.updateDevice(sDevice)
                    }

                    // Change Layout Type Button
                    btnToSliderMode.setOnClickListener {
                        mode = SLIDER
                        frameLayout.removeAllViews()
                        setupSpiderDetailsWithSliders(rootView, inflater)
                    }
                }
            }
        }

        frameLayout.addView(spiderControlsBinding.spiderDropControlsContent)
    }

    private fun setupLanternLayout(rootView: View, inflater: LayoutInflater) {
        when (mode) {
            PRECISION -> setupLanternDetailsWithEditTexts(rootView, inflater)
            SLIDER -> setupLanternDetailsWithSliders(rootView, inflater)
            else -> throw Exception("DeviceDetailsFragment: onCreateView - Unknown mode value: $mode")
        }
    }

    private fun onHelpButtonClicked(id: Int, variableNameStringId: Int? = null) {
        context?.let { c ->
            val helpDesc = device.getVariableDescription(c, id)
            val varNameId = variableNameStringId ?: R.string.help_title

            AlertDialog.Builder(c)
                    .setTitle(varNameId)
                    .setMessage(helpDesc)
                    .setPositiveButton(getString(R.string.dismiss)) { dialog, _ -> dialog?.dismiss() }
                    .create()
                    .show()
        }
    }

    private fun setupLanternDetailsWithSliders(rootView: View, inflater: LayoutInflater) {
        val frameLayout = rootView.findViewById<FrameLayout>(R.id.detailsContainer)
        val lanternDetailsBinding = ContentLanternControlsV2Binding.inflate(inflater)
        context?.let { c ->
            lanternDetailsBinding.apply {
                (device as LanternDevice).let { lantern ->
                    btnBack.setOnClickListener { goBack() }
                    etName.setText(lantern.name)
                    skSmoothing.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                            tvSmoothingValVal.text = progress.toString()
                        }
                        override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                        override fun onStopTrackingTouch(seekBar: SeekBar?) {}
                    })
                    skSmoothing.progress = lantern.smoothing

                    val dropDelayStepSize = 10
                    skDropDelay.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                            if (fromUser)
                                seekBar?.progress = (progress/dropDelayStepSize)*dropDelayStepSize
                            tvDropDelayVal.text = seekBar?.progress?.toString()
                        }
                        override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                        override fun onStopTrackingTouch(seekBar: SeekBar?) {}
                    })
                    skDropDelay.progress = lantern.dropDelay

                    skDropValue.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                            tvDropValVal.text = progress.toString()
                        }
                        override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                        override fun onStopTrackingTouch(seekBar: SeekBar?) {}
                    })
                    skDropValue.progress = lantern.dropValue

                    val flickerStepSize = 100
                    skRampDelay.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                            if (fromUser)
                                seekBar?.progress = (progress/flickerStepSize)*flickerStepSize
                            tvFlickerRateVal.text = seekBar?.progress?.toString()
                        }
                        override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                        override fun onStopTrackingTouch(seekBar: SeekBar?) {}
                    })
                    skRampDelay.progress = lantern.rampDelay

                    skMinBrightness.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                            tvMinBrightnessVal.text = progress.toString()
                        }
                        override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                        override fun onStopTrackingTouch(seekBar: SeekBar?) {}

                    })
                    skMinBrightness.progress = lantern.minBrightness

                    skMaxBrightness.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                            tvMaxBrightnessVal.text = progress.toString()
                        }

                        override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                        override fun onStopTrackingTouch(seekBar: SeekBar?) {}

                    })
                    skMaxBrightness.progress = lantern.maxBrightness

                    skFlickerDelayMin.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                            tvFlickerDelayMinVal.text = progress.toString()
                        }
                        override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                        override fun onStopTrackingTouch(seekBar: SeekBar?) {}

                    })
                    skFlickerDelayMin.progress = lantern.flickerDelayMin

                    skFlickerDelayMax.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                            tvFlickerDelayMaxVal.text = progress.toString()
                        }
                        override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                        override fun onStopTrackingTouch(seekBar: SeekBar?) {}

                    })
                    skFlickerDelayMax.progress = lantern.flickerDelayMax

                    ArrayAdapter.createFromResource(
                            c,
                            R.array.pin_values_all,
                            android.R.layout.simple_spinner_item
                    ).also { adapter ->
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        spnPin.adapter = adapter
                    }
                    spnPin.setSelection(posFromPin(device.pin))

                    btnUpdate.setOnClickListener {
                        lantern.smoothing = skSmoothing.progress
                        lantern.minBrightness = skMinBrightness.progress
                        lantern.maxBrightness = skMaxBrightness.progress
                        lantern.rampDelay = skRampDelay.progress
                        lantern.dropValue = skDropValue.progress
                        lantern.dropDelay = skDropDelay.progress
                        lantern.name = etName.text.toString()
                        lantern.flickerDelayMin = skFlickerDelayMin.progress
                        lantern.flickerDelayMax = skFlickerDelayMax.progress
                        lantern.pin = pinFromPos(spnPin.selectedItemPosition)

                        FirebaseDao.updateDevice(lantern)
                    }

                    btnDropDelayHelp.setOnClickListener { onHelpButtonClicked(it.id) }
                    btnDropValueHelp.setOnClickListener { onHelpButtonClicked(it.id) }
                    btnFlickerRateHelp.setOnClickListener { onHelpButtonClicked(it.id) }
                    btnMaxBrightnessHelp.setOnClickListener { onHelpButtonClicked(it.id) }
                    btnMinBrightnessHelp.setOnClickListener { onHelpButtonClicked(it.id) }
                    btnNameHelp.setOnClickListener { onHelpButtonClicked(it.id) }
                    btnPinHelp.setOnClickListener { onHelpButtonClicked(it.id) }
                    btnSmoothingHelp.setOnClickListener { onHelpButtonClicked(it.id) }

                    btnChangeToPreciseMode.setOnClickListener {
                        mode = PRECISION
                        frameLayout.removeAllViews()
                        setupLanternDetailsWithEditTexts(rootView, inflater)
                    }
                }
            }
        }

        frameLayout.addView(lanternDetailsBinding.lanternContentRoot)
    }

    private fun setupLanternDetailsWithEditTexts(rootView: View, inflater: LayoutInflater) {
        val frameLayout = rootView.findViewById<FrameLayout>(R.id.detailsContainer)
        val lanternDetailsBinding = ContentLanternControlsBinding.inflate(inflater)
        context?.let { c ->
            lanternDetailsBinding.apply {
                (device as LanternDevice).let { lantern ->
                    btnBack.setOnClickListener { goBack() }
                    etName.setText(lantern.name)
                    etDropDelay.setText(lantern.dropDelay.toString())
                    etDropValue.setText(lantern.dropValue.toString())
                    etRampDelay.setText(lantern.rampDelay.toString())
                    etMinBrightness.setText(lantern.minBrightness.toString())
                    etMaxBrightness.setText(lantern.maxBrightness.toString())
                    etSmoothing.setText(lantern.smoothing.toString())
                    etFlickerDelayMin.setText(lantern.flickerDelayMin.toString())
                    etFlickerDelayMax.setText(lantern.flickerDelayMax.toString())

                    ArrayAdapter.createFromResource(
                            c,
                            R.array.pin_values_all,
                            android.R.layout.simple_spinner_item
                    ).also { adapter ->
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        spnPin.adapter = adapter
                    }

                    spnPin.setSelection(posFromPin(device.pin))

                    btnUpdate.setOnClickListener {
                        lantern.smoothing = etSmoothing.text.toString().toInt()
                        lantern.minBrightness = etMinBrightness.text.toString().toInt()
                        lantern.maxBrightness = etMaxBrightness.text.toString().toInt()
                        lantern.rampDelay = etRampDelay.text.toString().toInt()
                        lantern.dropValue = etDropValue.text.toString().toInt()
                        lantern.dropDelay = etDropDelay.text.toString().toInt()
                        lantern.flickerDelayMin = etFlickerDelayMin.text.toString().toInt()
                        lantern.flickerDelayMax = etFlickerDelayMax.text.toString().toInt()
                        lantern.name = etName.text.toString()
                        lantern.pin = pinFromPos(spnPin.selectedItemPosition)

                        FirebaseDao.updateDevice(lantern)
                    }

                    btnDropDelayHelp.setOnClickListener { onHelpButtonClicked(it.id) }
                    btnDropValueHelp.setOnClickListener { onHelpButtonClicked(it.id) }
                    btnRampDelayHelp.setOnClickListener { onHelpButtonClicked(it.id) }
                    btnMaxBrightnessHelp.setOnClickListener { onHelpButtonClicked(it.id) }
                    btnMinBrightnessHelp.setOnClickListener { onHelpButtonClicked(it.id) }
                    btnNameHelp.setOnClickListener { onHelpButtonClicked(it.id) }
                    btnPinHelp.setOnClickListener { onHelpButtonClicked(it.id) }
                    btnSmoothingHelp.setOnClickListener { onHelpButtonClicked(it.id) }

                    btnToSliderMode.setOnClickListener {
                        mode = SLIDER
                        frameLayout.removeAllViews()
                        setupLanternDetailsWithSliders(rootView, inflater)
                    }
                }
            }
        }

        frameLayout.addView(lanternDetailsBinding.lanternContentRoot)
    }

    private fun posFromPin(pin: Int): Int {
        return if (pin == PIN_A0)
            0
        else
            pin
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                Log.d("TEST", "Capturing on back pressed!")
                goBack()
            }
        })
    }

    private fun goBack() {
        (activity as MainActivity?)?.isShowingDeviceDetails = false
        findNavController().navigate(R.id.action_ControlFragment_to_DevicesFragment)
    }

    private fun pinFromPos(position: Int): Int {
        return when (val resPin = resources.getStringArray(R.array.pin_values_all)[position]) {
            "A0" -> PIN_A0
            else -> resPin.toInt()
        }
    }


    companion object {
        const val PRECISION = 1
        const val SLIDER = 2
    }
}