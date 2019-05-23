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

private const val EXTRA_MAC_ADDRESS = "extra_mac_address"

class DeviceActivity : AppCompatActivity() {

    companion object {
        fun newInstance(context: Context, macAddress: String): Intent =
            Intent(context, DeviceActivity::class.java).apply { putExtra(EXTRA_MAC_ADDRESS, macAddress) }
    }

    private lateinit var macAddress: String
    private lateinit var bleDevice: RxBleDevice
    private lateinit var c : UUID
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_device)
        macAddress = intent.getStringExtra(EXTRA_MAC_ADDRESS)
        bleDevice = SampleApplication.rxBleClient.getBleDevice(macAddress)
        supportActionBar!!.subtitle = getString(R.string.mac_address, macAddress)
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
                                startActivity(CharacteristicOperationExampleActivity.newInstance(this, macAddress, characteristic.uuid))
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
