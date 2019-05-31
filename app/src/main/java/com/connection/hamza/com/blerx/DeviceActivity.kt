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
private const val EXTRA_SIZE = "extra_mac_address_size"

class DeviceActivity : AppCompatActivity() {

    companion object {
        fun newInstance(context: Context, macAddress: Array<String> , size : Int): Intent =
            Intent(context, DeviceActivity::class.java).apply {
                putExtra(EXTRA_MAC_ADDRESS, macAddress)
                putExtra(EXTRA_SIZE, size)
            }
    }

    private lateinit var macAddress: Array<String>
    private lateinit var bleDevice: RxBleDevice
    private lateinit var c : UUID

    private  var sizeArray : Int = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_device)
       // macAddress = intent.getStringExtra(EXTRA_MAC_ADDRESS)
        macAddress = (intent!!.getStringArrayExtra(EXTRA_MAC_ADDRESS))
        sizeArray = (intent!!.getIntExtra(EXTRA_SIZE,0))
        bleDevice = SampleApplication.rxBleClient.getBleDevice(macAddress[0])
       // supportActionBar!!.subtitle = getString(R.string.mac_address, macAddress)
        onConnectToggleClick()
    }

    @SuppressLint("CheckResult")
    private fun onConnectToggleClick() {
        Log.d("Logging","THIS IS FROM onConnecte Methos")
        bleDevice.establishConnection(false)
                .flatMapSingle { it.discoverServices() }
                .take(1) // Disconnect automatically after discovery
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe {}
                .doFinally {}
                .subscribe({
                    it.bluetoothGattServices.flatMap {service ->
                        service.characteristics.map { characteristic ->
                            if (characteristic.isNotifiable){
                                c= characteristic.uuid
                                startActivity(CharacteristicOperationExampleActivity.newInstance(this, macAddress, characteristic.uuid , sizeArray))
                                finish()
                                Log.d("DALIYO","macAdress =  $macAddress UUID =  ${service.uuid}")
                            }
                        }
                    }
                }, {

                })

    }

    private val BluetoothGattCharacteristic.isNotifiable: Boolean
        get() = properties and BluetoothGattCharacteristic.PROPERTY_NOTIFY != 0

}
