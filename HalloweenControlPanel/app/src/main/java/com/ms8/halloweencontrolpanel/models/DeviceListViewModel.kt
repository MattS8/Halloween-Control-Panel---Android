package com.ms8.halloweencontrolpanel.models

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ms8.halloweencontrolpanel.database.FirebaseDao
import com.ms8.halloweencontrolpanel.database.objects.Device

class DeviceListViewModel(): ViewModel(), FirebaseDao.DeviceListCallback {

    private val _navigateToDeviceDetail = MutableLiveData("")
    val navigateToDeviceDetail
        get() = _navigateToDeviceDetail

    private val _deviceList = MutableLiveData<Map<String, List<Device>>>()
    val deviceList
        get() = _deviceList

    private val _deviceListError = MutableLiveData<Exception?>()
    val deviceListError
        get() = _deviceListError

    init {
        FirebaseDao.listenForDevices(this)
    }

    fun onDeviceItemClicked(uid: String) {
        _navigateToDeviceDetail.value = uid
    }

    fun onDeviceDetailNavigated() {
        _navigateToDeviceDetail.value = null
    }

    override fun onDeviceListChanged(newList: Map<String, List<Device>>) {
        _deviceList.value = newList
    }

    override fun onDeviceListError(e: Exception) {
        _deviceListError.value = e
    }

}