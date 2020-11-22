package com.ms8.halloweencontrolpanel

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.FrameLayout
import android.widget.SeekBar
import androidx.activity.OnBackPressedCallback
import androidx.navigation.fragment.findNavController
import com.ms8.halloweencontrolpanel.database.FirebaseDao
import com.ms8.halloweencontrolpanel.database.objects.Device
import com.ms8.halloweencontrolpanel.database.objects.LanternDevice
import com.ms8.halloweencontrolpanel.databinding.ContentLanternControlsBinding
import com.ms8.halloweencontrolpanel.databinding.ContentLanternControlsV2Binding

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class DeviceDetailsFragment : Fragment() {
    var mode = PRECISION

    lateinit var device: Device

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("detailMode", mode)
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        mode = savedInstanceState?.getInt("detailMode") ?: mode

        val rootView = inflater.inflate(R.layout.fragment_device_controls, container, false)
        device = FirebaseDao.getDevice(arguments?.getString("DeviceUID") ?: throw Exception("Missing device UID argument!"))
            ?: throw Exception("Unable to retrieve details for device ${arguments?.getString("DeviceUID")}")

        when (device.groupName) {
            LanternDevice.GroupName -> {
                when (mode) {
                    PRECISION -> setupLanternDetailsWithEditTexts(rootView, inflater)
                    SLIDER -> setupLanternDetailsWithSliders(rootView, inflater)
                    else -> throw Exception("DeviceDetailsFragment: onCreateView - Unknown mode value: $mode")
                }
            }
            else -> {
                throw Exception("Unknown device type ${arguments?.get("DeviceType")}. Can't handle fragment inner view!")
            }
        }

        return rootView
    }

    private fun onHelpButtonClicked(id: Int) {
        val helpDesc = when (id) {
            R.id.btnDropDelayHelp -> getString(R.string.lanternDropDelayDesc)
            R.id.btnDropValueHelp -> getString(R.string.lanternDropValueDesc)
            R.id.btnRampDelayHelp -> getString(R.string.lanternFlickerRateDesc)
            R.id.btnMinBrightnessHelp -> getString(R.string.lanternMinBrightnessDesc)
            R.id.btnMaxBrightnessHelp -> getString(R.string.lanternMaxBrightnessDesc)
            R.id.btnPinHelp -> getString(R.string.lanternPinDesc)
            R.id.btnNameHelp -> getString(R.string.lanternNameDesc)
            R.id.btnSmoothingHelp -> getString(R.string.lanternSmoothingDesc)
            R.id.btnFlickerDelayMinHelp -> getString(R.string.lanternFlickerDelayMinDesc)
            R.id.btnFlickerDelayMaxHelp -> getString(R.string.lanternFlickerDelayMaxDesc)
            else -> ""
        }

        AlertDialog.Builder(requireContext())
                .setTitle(R.string.help_title)
                .setMessage(helpDesc)
                .setPositiveButton(getString(R.string.dismiss)) { dialog, _ -> dialog?.dismiss() }
                .create()
                .show()
    }

    private fun setupLanternDetailsWithSliders(rootView: View, inflater: LayoutInflater) {
        val frameLayout = rootView.findViewById<FrameLayout>(R.id.detailsContainer)
        val lanternDetailsBinding = ContentLanternControlsV2Binding.inflate(inflater)
        lanternDetailsBinding.apply {
            (device as LanternDevice).let { lantern ->
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
                        this@DeviceDetailsFragment.requireContext(),
                        R.array.pin_values,
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
        frameLayout.addView(lanternDetailsBinding.lanternContentRoot)
    }

    private fun setupLanternDetailsWithEditTexts(rootView: View, inflater: LayoutInflater) {
        val frameLayout = rootView.findViewById<FrameLayout>(R.id.detailsContainer)
        val lanternDetailsBinding = ContentLanternControlsBinding.inflate(inflater)
        lanternDetailsBinding.apply {
            (device as LanternDevice).let { lantern ->
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
                        this@DeviceDetailsFragment.requireContext(),
                        R.array.pin_values,
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
        frameLayout.addView(lanternDetailsBinding.lanternContentRoot)
    }

    private fun posFromPin(pin: Int): Int {
        resources.getStringArray(R.array.pin_values).forEachIndexed { index, pinStr ->
            if (pinStr.toInt() == pin)
                return index
        }

        return -1
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

    private fun pinFromPos(position: Int) = resources.getStringArray(R.array.pin_values)[position].toInt()


    companion object {
        const val PRECISION = 1
        const val SLIDER = 2
    }
}