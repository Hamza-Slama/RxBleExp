package com.connection.hamza.com.blerx.scanner

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.connection.hamza.com.blerx.R

import com.polidea.rxandroidble2.scan.ScanResult

internal class ScanResultsAdapter(
    private val onClickListener: (ScanResult) -> Unit
) : RecyclerView.Adapter<ScanResultsAdapter.ViewHolder>() {


    var arrList = ArrayList<String>()
    var setOfMacAdress = HashSet<String>()
    var setOfSensorNameA = ArrayList<String>()

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val device: TextView = itemView.findViewById(android.R.id.text1)
        val rssi: TextView = itemView.findViewById(android.R.id.text2)
        val check : CheckBox = itemView.findViewById(R.id.checkbox_scan_ble)
    }

    private val data = mutableListOf<ScanResult>()

    fun addScanResult(bleScanResult: ScanResult) {
        // Not the best way to ensure distinct devices, just for the sake of the demo.
        data.withIndex()
            .firstOrNull { it.value.bleDevice == bleScanResult.bleDevice }
            ?.let {
                // device already in data list => update
                data[it.index] = bleScanResult
                notifyItemChanged(it.index)
            }
            ?: run {
                // new device => add to data list
                with(data) {
                    add(bleScanResult)
                    sortBy { it.bleDevice.macAddress }
                }
                notifyDataSetChanged()
            }
    }

    fun clearScanResults() {
        data.clear()
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(data[position]) {
            holder.device.text = String.format("%s (%s)", bleDevice.macAddress, bleDevice.name)
            holder.rssi.text = String.format("RSSI: %d", rssi)
            holder.itemView.setOnClickListener { onClickListener(this) }
            if (holder.check.isChecked){
                setOfMacAdress.add(bleDevice.macAddress)
                setOfSensorNameA.add(bleDevice.name!!)
            }
            else {
                setOfMacAdress.remove(bleDevice.macAddress)
                setOfSensorNameA.remove(bleDevice.name!!)
               // setOfSensorName.remove(bleDevice.name!!)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_list_of_dispo_ble, parent, false)
            .let { ViewHolder(it) }
}
