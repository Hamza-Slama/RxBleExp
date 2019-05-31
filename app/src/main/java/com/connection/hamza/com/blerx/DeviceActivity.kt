package com.connection.hamza.com.blerx

import android.annotation.SuppressLint
import android.bluetooth.BluetoothGattCharacteristic
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.connection.hamza.com.blerx.charcartistics.CharacteristicOperationExampleActivity
import com.polidea.rxandroidble2.RxBleDevice
import io.reactivex.android.schedulers.AndroidSchedulers
import java.util.*
import kotlin.collections.ArrayList

private const val EXTRA_MAC_ADDRESS = "extra_mac_address"
private const val EXTRA_SENSOR_NAME = "extra_sensor_name"
private const val EXTRA_SIZE = "extra_mac_address_size"

class DeviceActivity : AppCompatActivity() {

    companion object {
        fun newInstance(context: Context, macAddress: Array<String>, sensorName: Array<String> , size : Int): Intent =
            Intent(context, DeviceActivity::class.java).apply {
                putExtra(EXTRA_MAC_ADDRESS, macAddress)
                putExtra(EXTRA_SENSOR_NAME, sensorName)
                putExtra(EXTRA_SIZE, size)
            }
    }

    private lateinit var macAddress: Array<String>
    private lateinit var sensorName: Array<String>
    private lateinit var bleDevice: RxBleDevice
    private lateinit var c : UUID

    private  var sizeArray : Int = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_device)
        macAddress = (intent!!.getStringArrayExtra(EXTRA_MAC_ADDRESS))
        Log.d("recbyte","Set arrayOfMacAdress  From Device= "+(Arrays.toString(macAddress)))

        sensorName = (intent!!.getStringArrayExtra(EXTRA_SENSOR_NAME))
        Log.d("recbyte","Set arrayOfMacAdress  From Device= "+(Arrays.toString(sensorName)))
        sizeArray = (intent!!.getIntExtra(EXTRA_SIZE,0))
        bleDevice = SampleApplication.rxBleClient.getBleDevice(macAddress[0])
       // supportActionBar!!.subtitle = getString(R.string.mac_address, macAddress)
        startActivity(CharacteristicOperationExampleActivity.newInstance(this, macAddress, sensorName ,  sizeArray))
    }

}
