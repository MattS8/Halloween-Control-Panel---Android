package com.ms8.halloweencontrolpanel

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.FrameLayout
import androidx.activity.OnBackPressedCallback
import androidx.navigation.fragment.findNavController
import com.ms8.halloweencontrolpanel.database.FirebaseDao
import com.ms8.halloweencontrolpanel.database.objects.Device
import com.ms8.halloweencontrolpanel.database.objects.LanternDevice
import com.ms8.halloweencontrolpanel.databinding.ContentLanternControlsBinding

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class SecondFragment : Fragment() {
    lateinit var device: Device
    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        val rootView = inflater.inflate(R.layout.fragment_device_controls, container, false)
        device = FirebaseDao.getDevice(arguments?.getString("DeviceUID") ?: throw Exception("Missing device UID argument!"))
            ?: throw Exception("Unable to retrieve details for device ${arguments?.getString("DeviceUID")}")

        when (device.groupName) {
            LanternDevice.GroupName -> {
                val frameLayout = rootView.findViewById<FrameLayout>(R.id.detailsContainer)
                val lanternDetailsBinding = ContentLanternControlsBinding.inflate(inflater)
                lanternDetailsBinding.apply {
                    (device as LanternDevice).let { lantern ->
                        etName.setText(lantern.name)
                        etDropDelay.setText(lantern.dropDelay.toString())
                        etDropValue.setText(lantern.dropValue.toString())
                        etFlickerRate.setText(lantern.flickerRate.toString())
                        etMinBrightness.setText(lantern.minBrightness.toString())
                        etMaxBrightness.setText(lantern.maxBrightness.toString())
                        etSmoothing.setText(lantern.smoothing.toString())

                        ArrayAdapter.createFromResource(
                            this@SecondFragment.requireContext(),
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
                            lantern.flickerRate = etFlickerRate.text.toString().toInt()
                            lantern.dropValue = etDropValue.text.toString().toInt()
                            lantern.dropDelay = etDropDelay.text.toString().toInt()
                            lantern.name = etName.text.toString()
                            lantern.pin = pinFromPos(spnPin.selectedItemPosition)

                            FirebaseDao.updateDevice(lantern)
                        }
                    }
                }
                frameLayout.addView(lanternDetailsBinding.lanternContentRoot)
            }
            else -> {
                throw Exception("Unknown device type ${arguments?.get("DeviceType")}. Can't handle fragment inner view!")
            }
        }

        return rootView
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

}