package com.connection.hamza.com.blerx.scanner

//import android.support.v7.app.AppCompatActivity
import android.bluetooth.BluetoothGattCharacteristic
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity

import com.connection.hamza.com.blerx.DeviceActivity
import com.connection.hamza.com.blerx.R
import com.connection.hamza.com.blerx.SampleApplication
import com.connection.hamza.com.blerx.charcartistics.CharacteristicOperationExampleActivity
import com.connection.hamza.com.blerx.util.isLocationPermissionGranted
import com.connection.hamza.com.blerx.util.requestLocationPermission
import com.connection.hamza.com.blerx.util.showError
import com.polidea.rxandroidble2.RxBleDevice
import com.polidea.rxandroidble2.exceptions.BleScanException
import com.polidea.rxandroidble2.scan.ScanFilter
import com.polidea.rxandroidble2.scan.ScanResult
import com.polidea.rxandroidble2.scan.ScanSettings

import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.activity_main2.*
import java.util.*

class ScanActivity : AppCompatActivity() {

    private val rxBleClient = SampleApplication.rxBleClient

    private var scanDisposable: Disposable? = null
    private lateinit var bleDevice: RxBleDevice
private lateinit var macAddress: Array<String>
    //TODO : Add your Sensor Name to filter BLE
    private var sensorName = ""
    private val resultsAdapter =
            ScanResultsAdapter {}

    private var hasClickedScan = false

    private val isScanning: Boolean
        get() = scanDisposable != null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)
        configureResultList()
        scan_toggle_btn.setOnClickListener { onScanToggleClick() }
        onScanToggleClick()

        btn_lunch_activity.setOnClickListener {
            val arrayOfMacAdress = Array<String>(resultsAdapter.setOfMacAdress.size){""}
            resultsAdapter.setOfMacAdress.toArray(arrayOfMacAdress)
            Log.d("recbyte","Set arrayOfMacAdress = "+(Arrays.toString(arrayOfMacAdress)))


            val array = Array<String>(resultsAdapter.setOfSensorNameA.size){""}
            resultsAdapter.setOfSensorNameA.toArray(array)
            val set = HashSet(Arrays.asList(*array))
            val setOfSensorNameA = Array<String>(set.size){""}
            set.toArray(setOfSensorNameA)
            Log.d("recbyte","Arra name  = "+(Arrays.toString(setOfSensorNameA)))
            startActivity(DeviceActivity.newInstance(this, arrayOfMacAdress ,setOfSensorNameA , arrayOfMacAdress.size))
        }
    }

    private fun configureResultList() {
        with(scan_results) {
            setHasFixedSize(true)
            itemAnimator = null
            adapter = resultsAdapter
        }
    }

    private fun onScanToggleClick() {
        if (isScanning) {
            scanDisposable?.dispose()
        } else {
            if (isLocationPermissionGranted()) {
                scanBleDevices()
                        .observeOn(AndroidSchedulers.mainThread())
                        .doFinally { dispose() }
                        .subscribe({
                            if ((! it.bleDevice.name.equals(null)) && (it.bleDevice.name!!.contains(sensorName)) ){
//                            Log.d("BLENAME","ok  = ${it.bleDevice.name}")
                           resultsAdapter.addScanResult(it)
                            }
                        },
                                { onScanFailure(it) })
                        .let { scanDisposable = it }
            } else {
                hasClickedScan = true
                requestLocationPermission()
            }
        }
       // updateButtonUIState()
    }

    private fun scanBleDevices(): Observable<ScanResult> {
        val scanSettings = ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
                .build()

        val scanFilter = ScanFilter.Builder()

//            .setDeviceAddress("B4:99:4C:34:DC:8B")
                // add custom filters if needed
                .build()

        return rxBleClient.scanBleDevices(scanSettings, scanFilter)
    }

    private fun dispose() {
        scanDisposable = null
        resultsAdapter.clearScanResults()
        //updateButtonUIState()
    }

    private fun onScanFailure(throwable: Throwable) {
        if (throwable is BleScanException) showError(throwable)
    }

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
                                startActivity(CharacteristicOperationExampleActivity.newInstance(this, macAddress, characteristic.uuid,0))
                               // Log.d("DALIYO","macAdress =  $macAddress UUID =  ${service.uuid}")
                            }

                        }
                    }

                }, {

                })
                .let {

                }
    }



    private val BluetoothGattCharacteristic.isNotifiable: Boolean
        get() = properties and BluetoothGattCharacteristic.PROPERTY_NOTIFY != 0
    public override fun onPause() {
        super.onPause()
        // Stop scanning in onPause callback.
        if (isScanning) scanDisposable?.dispose()
    }
}
