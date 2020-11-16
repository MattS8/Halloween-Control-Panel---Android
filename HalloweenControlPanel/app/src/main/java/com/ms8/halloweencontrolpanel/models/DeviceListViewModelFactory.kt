package com.ms8.halloweencontrolpanel.models

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class DeviceListViewModelFactory(
    private val application: Application
    ) : ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        @Suppress("unchecked_cast")
        if (modelClass.isAssignableFrom(DeviceListViewModel::class.java))
            return DeviceListViewModel() as T
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}