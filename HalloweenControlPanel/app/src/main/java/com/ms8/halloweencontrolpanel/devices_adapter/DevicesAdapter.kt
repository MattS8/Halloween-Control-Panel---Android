package com.ms8.halloweencontrolpanel.devices_adapter


import android.animation.ObjectAnimator
import android.util.ArrayMap
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ms8.halloweencontrolpanel.R
import com.ms8.halloweencontrolpanel.database.objects.Device


class DevicesAdapter(val deviceClickListener: DeviceClickListener,
                     val headerClickListener: HeaderClickListener = object : HeaderClickListener {
                         override fun onClick(deviceGroup: DataGroup) { }

})
    : ListAdapter<DevicesAdapter.DataItem, RecyclerView.ViewHolder>(DeviceDiffCallback()) {

    private val allItems: MutableMap<String, DataGroup> = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
        ArrayMap()
    } else {
        HashMap()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_HEADER -> {
                HeaderViewHolder.from(parent)
            }
            TYPE_ITEM -> {
                DeviceViewHolder.from(parent)
            }
            else -> {
                throw ClassCastException("Unknown viewType: $viewType")
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is DataItem.DeviceHeader -> TYPE_HEADER
            is DataItem.DeviceItem -> TYPE_ITEM
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is DeviceViewHolder -> {
                val deviceItem = getItem(position) as DataItem.DeviceItem
                holder.bind(deviceItem.deviceInfo, deviceClickListener)
            }
            is HeaderViewHolder -> {
                val headerItem = getItem(position) as DataItem.DeviceHeader
                holder.bind(headerItem.headerString)
//                allItems[headerItem.headerString]?.let {
//                    holder.bind(it, headerClickListener)
//                }
            }
        }
    }

    fun addDeviceGroup(header: String, list: List<Device>?, isHidden: Boolean = false) {
        allItems[header] = DataGroup(header, list?.map { DataItem.DeviceItem(it) }, isHidden)
        updateList()
    }

    fun removeDeviceGroup(header: String) {
        allItems.remove(header)
        updateList()
    }

    fun updateList() {
        val flatList : ArrayList<DataItem> = ArrayList()

        allItems.keys.forEach { key ->
            if (allItems[key]?.isHidden != true) {
                flatList.add(DataItem.DeviceHeader(key))
                allItems[key]?.dataItems?.let {
                    flatList.addAll(it)
                }
            }
        }

        submitList(flatList)
    }

    fun setDeviceList(devices: Map<String, List<Device>>) {
        val flatList: ArrayList<DataItem> = ArrayList()
        devices.keys.forEach { key ->
            Log.d("TEST", "adding header for key $key")
            devices[key]?.let { list ->
                flatList.add(DataItem.DeviceHeader(key))
                flatList.addAll(list.map { DataItem.DeviceItem(it) })
            }
        }

        submitList(flatList)
    }

    class DeviceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(device: Device, listener: DeviceClickListener) {
            itemView.findViewById<TextView>(R.id.tvDeviceName).text = device.name
            itemView.setOnClickListener { listener.onClick(device) }
        }

        companion object {
            fun from(parent: ViewGroup) : DeviceViewHolder {
                return DeviceViewHolder(
                    LayoutInflater.from(parent.context).inflate(
                        R.layout.itm_device,
                        parent,
                        false
                    )
                )
            }
        }
    }

    class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        //private var rotationAngle : Float = 0f

        fun bind(headerName: String) {
            itemView.findViewById<TextView>(R.id.tvHeader).text = headerName
        }

        fun bind(deviceGroup: DataGroup, listener: HeaderClickListener) {
            itemView.findViewById<TextView>(R.id.tvHeader).text = deviceGroup.groupName
//            val expandCollapseView = itemView.findViewById<ImageView>(R.id.imgExpandCollapseBtn)
//            itemView.setOnClickListener {
//                ObjectAnimator.ofFloat()
//                val anim: ObjectAnimator =
//                    ObjectAnimator.ofFloat(expandCollapseView, "rotation", rotationAngle, rotationAngle + 180)
//                anim.duration = 500
//                anim.start()
//                rotationAngle += 180
//                rotationAngle %= 360
//
//                deviceGroup.isHidden = !deviceGroup.isHidden
//
//                listener.onClick(deviceGroup)
//            }
        }

        companion object {
            fun from(parent: ViewGroup) : HeaderViewHolder {
                return HeaderViewHolder(
                    LayoutInflater.from(parent.context).inflate(
                        R.layout.itm_device_header,
                        parent,
                        false
                    )
                )
            }
        }
    }

    class DeviceDiffCallback : DiffUtil.ItemCallback<DataItem>() {
        override fun areItemsTheSame(oldItem: DataItem, newItem: DataItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: DataItem, newItem: DataItem): Boolean {
            return oldItem == newItem
        }
    }

    data class DataGroup(
        val groupName: String,
        val dataItems: List<DataItem>?,
        var isHidden: Boolean = false
    )

    sealed class DataItem {
        abstract val id: String

        data class DeviceItem(val deviceInfo: Device): DataItem() {
            override val id = deviceInfo.uid
        }

        data class DeviceHeader(val headerString: String): DataItem() {
            override val id = headerString
        }
    }

    companion object {
        val TAG: String = DevicesAdapter::class.java.name
        const val TYPE_HEADER = 1
        const val TYPE_ITEM = 2
    }

    interface DeviceClickListener {
        fun onClick(device: Device)
    }

    interface HeaderClickListener {
        fun onClick(deviceGroup: DataGroup)
    }
}