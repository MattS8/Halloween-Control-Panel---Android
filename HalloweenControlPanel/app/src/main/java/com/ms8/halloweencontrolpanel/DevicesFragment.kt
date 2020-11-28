package com.ms8.halloweencontrolpanel

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.ms8.halloweencontrolpanel.database.objects.Device
import com.ms8.halloweencontrolpanel.databinding.FragmentDevicesBinding
import com.ms8.halloweencontrolpanel.devices_adapter.DevicesAdapter
import com.ms8.halloweencontrolpanel.devices_adapter.DevicesAdapter.Companion.TYPE_HEADER
import com.ms8.halloweencontrolpanel.models.DeviceListViewModel
import com.ms8.halloweencontrolpanel.models.DeviceListViewModelFactory


/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class DevicesFragment : Fragment(), DevicesAdapter.DeviceClickListener, DevicesAdapter.HeaderClickListener {
    lateinit var binding: FragmentDevicesBinding
    private var deviceListViewModel: DeviceListViewModel? = null

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentDevicesBinding.inflate(inflater, container, false)

        val application = requireNotNull(this.activity).application

        //val dataSource = DeviceDatabase.getInstance(application).deviceDatabaseDao
        val viewModelFactory = DeviceListViewModelFactory(application)

        deviceListViewModel = ViewModelProvider(this, viewModelFactory)
            .get(DeviceListViewModel::class.java)

        binding.rvDevices.adapter = DevicesAdapter(this, this)

        binding.lifecycleOwner = viewLifecycleOwner

        deviceListViewModel?.navigateToDeviceDetail?.observe(viewLifecycleOwner, { deviceUID ->
            if (deviceUID == "")
                return@observe

            (activity as MainActivity?)?.isShowingDeviceDetails = true
            this.findNavController().navigate(
                R.id.action_DevicesFragment_to_ControlFragment,
                Bundle().apply {
                    putString("DeviceUID", deviceUID)
                }
            )
            deviceListViewModel?.navigateToDeviceDetail?.value = ""
        })

        deviceListViewModel?.deviceList?.observe(viewLifecycleOwner, {devices ->
            (binding.rvDevices.adapter as DevicesAdapter).setDeviceList(devices)
        })

        binding.rvDevices.layoutManager = GridLayoutManager(this.context, 2, GridLayoutManager.VERTICAL, false)
        (binding.rvDevices.layoutManager as GridLayoutManager).spanSizeLookup = object :
            GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return when ((binding.rvDevices.adapter as DevicesAdapter).getItemViewType(position)) {
                    TYPE_HEADER -> 2
                    else -> 2
                }
            }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        view.findViewById<Button>(R.id.button_first).setOnClickListener {
//            findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
//        }
    }

    override fun onClick(device: Device) {
        Log.d(TAG, "${device.name} item was clicked!")
        deviceListViewModel?.onDeviceItemClicked(device.uid)
    }

    override fun onClick(deviceGroup: DevicesAdapter.DataGroup) {
        Log.d(TAG, "${deviceGroup.groupName} header was clicked!")
        //(binding.rvDevices.adapter as DevicesAdapter).updateList()
    }

    companion object {
        val TAG = DevicesFragment::class.java.name
    }

}