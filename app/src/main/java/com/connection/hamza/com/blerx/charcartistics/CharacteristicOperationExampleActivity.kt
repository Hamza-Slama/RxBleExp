package com.connection.hamza.com.blerx.charcartistics

import android.annotation.SuppressLint
import android.bluetooth.BluetoothGattCharacteristic
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.connection.hamza.com.blerx.R
import com.connection.hamza.com.blerx.SampleApplication
import com.connection.hamza.com.blerx.util.hasProperty
import com.connection.hamza.com.blerx.util.isConnected
import com.connection.hamza.com.blerx.util.toHex
import com.jakewharton.rx.ReplayingShare
import com.polidea.rxandroidble2.RxBleConnection
import com.polidea.rxandroidble2.RxBleDevice
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.activity_save.*
import java.util.*
import kotlin.collections.ArrayList


private const val EXTRA_MAC_ADDRESS = "extra_mac_address"

private const val EXTRA_SIZE = "extra_mac_address_size"

private const val EXTRA_SENSOR_NAME = "extra_sensor_name"

class CharacteristicOperationExampleActivity : AppCompatActivity() {

    companion object {
        fun newInstance(context: Context, macAddress: Array<String>, sensorName : Array<String>, size : Int) =
            Intent(context, CharacteristicOperationExampleActivity::class.java).apply {
                putExtra(EXTRA_MAC_ADDRESS, macAddress)
                putExtra(EXTRA_SENSOR_NAME, sensorName)
                putExtra(EXTRA_SIZE, size   )
            }
    }

    private lateinit var bleDevice: ArrayList<RxBleDevice>


    private lateinit var characteristicUuidArray: ArrayList<UUID>

    private val disconnectTriggerSubject = PublishSubject.create<Unit>()

    private lateinit var connectionObservableArray: ArrayList<Observable<RxBleConnection>>

    private val connectionDisposableArray : ArrayList<CompositeDisposable> = ArrayList()


    private lateinit var macAddress : Array<String>
    private lateinit var sensorName : Array<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_save)

        macAddress = (intent!!.getStringArrayExtra(EXTRA_MAC_ADDRESS))
        sensorName = (intent!!.getStringArrayExtra(EXTRA_SENSOR_NAME))
        Log.d("recbyte","Set arrayOfMacAdress  From Device= "+(Arrays.toString(sensorName)))
        val size = intent.getIntExtra(EXTRA_SIZE,0)
        Log.d("recbyte","size = $size")
        bleDevice = ArrayList()
        characteristicUuidArray = ArrayList()
        for ( i in 0 .. size-1) bleDevice.add(SampleApplication.rxBleClient.getBleDevice(macAddress[i]))
        onConnectToggleClickGetChar()
        connectionObservableArray = prepareConnectionObservable()
        supportActionBar!!.subtitle = getString(R.string.mac_address, macAddress[0])
        connect.setOnClickListener { onConnectToggleClick() }
        notify.setOnClickListener { onNotifyClick() }
    }

    private fun onNotifyClick() {
        for (i in 0 .. bleDevice.size-1)
        if (bleDevice[i].isConnected) {
            connectionObservableArray[i]
                .flatMap {
                    it.setupNotification(characteristicUuidArray[i]) }
                .doOnNext { runOnUiThread { notificationHasBeenSetUp() } }
                .flatMap { it }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    Log.d("rec_byte","i =   $i")
                    Log.d("rec_byte","${it.toHex()}")
                    Log.d("rec_byte","Device 2  = ${it.size}")
                    onNotificationReceived(it,"Device 2 ") }, { onNotificationSetupFailure(it) })
//                .let { connectionDisposableArray[i].add(it) }
        }

    }



    private fun onConnectionFailure(throwable: Throwable) = updateUI(null)




    private fun onNotificationReceived(bytes: ByteArray,name : String){}

    private fun onNotificationSetupFailure(throwable: Throwable) {}

    private fun notificationHasBeenSetUp() {}


    private fun updateUI(characteristic: BluetoothGattCharacteristic?) {
        if (characteristic == null) {
            connect.setText(R.string.button_connect)
            notify.isEnabled = false
        } else {
            connect.setText(R.string.button_disconnect)
            with(characteristic) {
                notify.isEnabled = hasProperty(BluetoothGattCharacteristic.PROPERTY_NOTIFY)
            }
        }
    }

    override fun onPause() {
        super.onPause()
      //  connectionDisposable2.clear()
    }

    private fun triggerDisconnect() = disconnectTriggerSubject.onNext(Unit)

    @SuppressLint("CheckResult")
    private fun onConnectToggleClickGetChar() {
        Log.d("Logging","THIS IS FROM onConnecte Methos")
        for ( i in 0 .. bleDevice.size-1)
        bleDevice[i].establishConnection(false)
                .flatMapSingle { it.discoverServices() }
                .take(1) // Disconnect automatically after discovery
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe {}
                .doFinally {}
                .subscribe({
                    // resultsAdapter.swapScanResult(it)
                    it.bluetoothGattServices.flatMap {service ->
                        service.characteristics.map { characteristic ->
                            if (characteristic.isNotifiable){
                                characteristicUuidArray.add( characteristic.uuid)
                                Log.d("rec_byte","macAdress $i =  ${macAddress[i]} UUID 2=  ${characteristicUuidArray[i]}")
                            }
                        }
                    }
                }, {})

    }

    private val BluetoothGattCharacteristic.isNotifiable: Boolean
        get() = properties and BluetoothGattCharacteristic.PROPERTY_NOTIFY != 0

    @SuppressLint("CheckResult")
    private fun prepareConnectionObservable():ArrayList< Observable<RxBleConnection>> {
        var arr = ArrayList<Observable<RxBleConnection>>()
            for ( i in 0 .. bleDevice.size-1){
            arr.add(bleDevice[i]
                    .establishConnection(false)
                    .takeUntil(disconnectTriggerSubject)
                    .compose(ReplayingShare.instance()))

            }
        return arr
    }
    private fun onConnectToggleClick() {
        for ( i in 0 .. bleDevice.size-1){


        if (bleDevice[i].isConnected) {
            triggerDisconnect()
        } else {
            connectionObservableArray[i]
                    .flatMapSingle { it.discoverServices() }
                    .flatMapSingle { it.getCharacteristic(characteristicUuidArray[i]) }
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnSubscribe { connect.setText(R.string.connecting) }
                    .subscribe(
                            { characteristic ->
                                updateUI(characteristic)
                                Log.i("rec_byte", "Hey, connection has been established! to macAddress $i =   ${macAddress[i]}" )
                            },
                            { onConnectionFailure(it) },
                            { updateUI(null) }
                    )
//                    .let { connectionDisposableArray[i].add(it) }
        }
    }
    }




}
