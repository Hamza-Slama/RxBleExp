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
import com.connection.hamza.com.blerx.util.disConnected
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
import java.util.UUID




private const val EXTRA_MAC_ADDRESS = "extra_mac_address"

private const val EXTRA_CHARACTERISTIC_UUID = "extra_uuid"

private const val EXTRA_SIZE = "extra_mac_address_size"


class CharacteristicOperationExampleActivity : AppCompatActivity() {

    companion object {
        fun newInstance(context: Context, macAddress: Array<String>, uuid: UUID , size : Int) =
            Intent(context, CharacteristicOperationExampleActivity::class.java).apply {
                putExtra(EXTRA_MAC_ADDRESS, macAddress)
                putExtra(EXTRA_CHARACTERISTIC_UUID, uuid)
                putExtra(EXTRA_SIZE, size   )
            }
    }

    private lateinit var characteristicUuid: UUID

    private val disconnectTriggerSubject = PublishSubject.create<Unit>()

    private lateinit var connectionObservable: Observable<RxBleConnection>

    private val connectionDisposable = CompositeDisposable()

    private lateinit var bleDevice: RxBleDevice

    /*
    Client 2
     */
    private lateinit var bleDevice2: RxBleDevice
    private lateinit var characteristicUuid2: UUID

    private val disconnectTriggerSubject2 = PublishSubject.create<Unit>()

    private lateinit var connectionObservable2: Observable<RxBleConnection>

    private val connectionDisposable2 = CompositeDisposable()
    //Kayak -- 20
    private val macAddress2 = "F6:B9:90:6F:67:32"


    /**
     * Client 3
     */
    private lateinit var bleDevice3: RxBleDevice
    private lateinit var characteristicUuid3: UUID

    private val disconnectTriggerSubject3 = PublishSubject.create<Unit>()

    private lateinit var connectionObservable3: Observable<RxBleConnection>

    private val connectionDisposable3 = CompositeDisposable()
    //Tir 14 -- 18
    private val macAddress3 = "EF:43:19:25:D2:1D"


    /**
     * Client 4
     */

    private lateinit var bleDevice4: RxBleDevice
    private lateinit var characteristicUuid4: UUID

    private val disconnectTriggerSubject4 = PublishSubject.create<Unit>()

    private lateinit var connectionObservable4: Observable<RxBleConnection>

    private val connectionDisposable4 = CompositeDisposable()
    //Crampon -- 20
    private val macAddress4 = "E0:0C:1D:77:8F:13"

    /**
     * Client 5
     */

    private lateinit var bleDevice5: RxBleDevice
    private lateinit var characteristicUuid5: UUID

    private val disconnectTriggerSubject5 = PublishSubject.create<Unit>()

    private lateinit var connectionObservable5: Observable<RxBleConnection>

    private val connectionDisposable5 = CompositeDisposable()
    //PPM104
    private val macAddress5 = "EE:05:9B:04:37:A2"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_save)

        val  macAddress = (intent!!.getStringArrayExtra(EXTRA_MAC_ADDRESS))
        val size = intent.getIntExtra(EXTRA_SIZE,0)
        Log.d("recbyte","size = $size")
        characteristicUuid = intent.getSerializableExtra(EXTRA_CHARACTERISTIC_UUID) as UUID
        Log.d("TAG","UUID = $characteristicUuid")
        bleDevice = SampleApplication.rxBleClient.getBleDevice(macAddress[0])
        connectionObservable = prepareConnectionObservable()
        supportActionBar!!.subtitle = getString(R.string.mac_address, macAddress[0])


       /*bleDevice2 = SampleApplication.rxBleClient.getBleDevice(macAddress2)
        onConnectToggleClickGetChar()
        connectionObservable2 = prepareConnectionObservable2()

        bleDevice3 = SampleApplication.rxBleClient.getBleDevice(macAddress3)
        onConnectToggleClickGetChar3()
        connectionObservable3 = prepareConnectionObservable3()


        /*bleDevice4 = SampleApplication.rxBleClient.getBleDevice(macAddress4)
        onConnectToggleClickGetChar4()
        connectionObservable4 = prepareConnectionObservable4()*/

        bleDevice5 = SampleApplication.rxBleClient.getBleDevice(macAddress5)
        onConnectToggleClickGetChar5()
        connectionObservable5 = prepareConnectionObservable5()*/

        connect.setOnClickListener {
            onConnectToggleClick()
            /*onConnectToggleClick2()
            onConnectToggleClick3()
            //onConnectToggleClick4()
            onConnectToggleClick5()*/
        }
        notify.setOnClickListener {
            onNotifyClick()
           /* onNotifyClick2()
            onNotifyClick3()
           // onNotifyClick4()
            onNotifyClick5()*/
        }
    }

    private fun prepareConnectionObservable(): Observable<RxBleConnection> =
        bleDevice
            .establishConnection(true)
            .takeUntil(disconnectTriggerSubject)
            .compose(ReplayingShare.instance())

    private fun onConnectToggleClick() {
        if (bleDevice.isConnected) {
            triggerDisconnect()
        } else {
            connectionObservable
                .flatMapSingle { it.discoverServices() }
                .flatMapSingle { it.getCharacteristic(characteristicUuid) }
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { connect.setText(R.string.connecting) }
                .subscribe(
                    { characteristic ->
                        updateUI(characteristic)
                        Log.i("rec_byte", "Hey, connection has been established! from macAdress 1")
                    },
                    { onConnectionFailure(it) },
                    { updateUI(null) }
                )
                .let { connectionDisposable.add(it) }
        }
    }


    private fun onNotifyClick() {
        if (bleDevice.isConnected) {
            connectionObservable
                .flatMap {
                    it.setupNotification(characteristicUuid) }
                .doOnNext { runOnUiThread { notificationHasBeenSetUp() } }
                .flatMap { it }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    Log.d("rec_byte","${it.toHex()}")
                    Log.d("rec_byte","Device 1 = ${it.size}")
                    onNotificationReceived(it,"Device 1 ") }, { onNotificationSetupFailure(it) })
                .let { connectionDisposable.add(it) }
        }
    }

    private fun onNotifyClick2() {
        if (bleDevice2.isConnected) {
            connectionObservable2
                .flatMap {
                    it.setupNotification(characteristicUuid2) }
                .doOnNext { runOnUiThread { notificationHasBeenSetUp() } }
                .flatMap { it }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    Log.d("rec_byte","${it.toHex()}")
                    Log.d("rec_byte","Device 2  = ${it.size}")
                    onNotificationReceived(it,"Device 2 ") }, { onNotificationSetupFailure(it) })
                .let { connectionDisposable2.add(it) }
        }
    }

    private fun onNotifyClick3() {
        if (bleDevice3.isConnected) {
            connectionObservable3
                    .flatMap {
                        it.setupNotification(characteristicUuid3) }
                    .doOnNext { runOnUiThread { notificationHasBeenSetUp() } }
                    .flatMap { it }
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        Log.d("rec_byte","${it.toHex()}")
                        Log.d("rec_byte","Device 3  = ${it.size}")
                        onNotificationReceived(it,"Device 3 ") }, { onNotificationSetupFailure(it) })
                    .let { connectionDisposable3.add(it) }
        }
    }

    private fun onNotifyClick4() {
        if (bleDevice4.isConnected) {
            connectionObservable4
                    .flatMap {
                        it.setupNotification(characteristicUuid4) }
                    .doOnNext { runOnUiThread { notificationHasBeenSetUp() } }
                    .flatMap { it }
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        Log.d("rec_byte","${it.toHex()}")
                        Log.d("rec_byte","Device 4  = ${it.size}")
                        onNotificationReceived(it,"Device 4 ") }, { onNotificationSetupFailure(it) })
                    .let { connectionDisposable4.add(it) }
        }
    }

    private fun onNotifyClick5() {
        if (bleDevice5.isConnected) {
            connectionObservable5
                    .flatMap {
                        it.setupNotification(characteristicUuid5) }
                    .doOnNext { runOnUiThread { notificationHasBeenSetUp() } }
                    .flatMap { it }
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        Log.d("rec_byte","${it.toHex()}")
                        Log.d("rec_byte","Device 5  = ${it.size}")
                        onNotificationReceived(it,"Device 5 ") }, { onNotificationSetupFailure(it) })
                    .let { connectionDisposable5.add(it) }
        }
    }

    private fun onConnectionFailure(throwable: Throwable) {
        updateUI(null)
    }



    private fun onNotificationReceived(bytes: ByteArray,name : String){
//        if (bytes.size == 20 ) {
//            setUp20Paq(bytes)
//        }
    }

    private fun onNotificationSetupFailure(throwable: Throwable) {
        //showSnackbarShort("Notifications error: $throwable")
    }


    private fun notificationHasBeenSetUp() {
      //showSnackbarShort("Notifications has been set up")
    }

    private fun triggerDisconnect() = disconnectTriggerSubject.onNext(Unit)


    /**
     * This method updates the UI to a proper state.
     *
     * @param characteristic a nullable [BluetoothGattCharacteristic]. If it is null then UI is assuming a disconnected state.
     */
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
        connectionDisposable.clear()
        connectionDisposable2.clear()
    }


    /**
    Client 2
     */

    private fun triggerDisconnect2() = disconnectTriggerSubject2.onNext(Unit)

    @SuppressLint("CheckResult")
    private fun onConnectToggleClickGetChar() {
        Log.d("Logging","THIS IS FROM onConnecte Methos")
        bleDevice2.establishConnection(false)
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
                                characteristicUuid2= characteristic.uuid
                                Log.d("rec_byte","macAdress2 =  $macAddress2 UUID 2=  ${characteristicUuid2}")
                            }
                        }
                    }
                }, {})

    }

    private val BluetoothGattCharacteristic.isNotifiable: Boolean
        get() = properties and BluetoothGattCharacteristic.PROPERTY_NOTIFY != 0

    private fun prepareConnectionObservable2(): Observable<RxBleConnection> =
            bleDevice2
                    .establishConnection(false)
                    .takeUntil(disconnectTriggerSubject2)
                    .compose(ReplayingShare.instance())


    private fun onConnectToggleClick2() {
        if (bleDevice2.isConnected) {
            triggerDisconnect2()
        } else {
            connectionObservable2
                    .flatMapSingle { it.discoverServices() }
                    .flatMapSingle { it.getCharacteristic(characteristicUuid2) }
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnSubscribe { connect.setText(R.string.connecting) }
                    .subscribe(
                            { characteristic ->
                                updateUI(characteristic)
                                Log.i("rec_byte", "Hey, connection has been established! to macAddress2 =   $macAddress2" )
                            },
                            { onConnectionFailure(it) },
                            { updateUI(null) }
                    )
                    .let { connectionDisposable2.add(it) }
        }
    }

 /**
    Client 3
     */

    private fun triggerDisconnect3() = disconnectTriggerSubject3.onNext(Unit)


    @SuppressLint("CheckResult")
    private fun onConnectToggleClickGetChar3() {
        Log.d("Logging","THIS IS FROM onConnecte Methos")
        bleDevice3.establishConnection(false)
                .flatMapSingle { it.discoverServices() }
                .take(1) // Disconnect automatically after discovery
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe {}
                .doFinally {}
                .subscribe({ it.bluetoothGattServices.flatMap {service ->
                                    service.characteristics.map { characteristic ->
                                            if (characteristic.isNotifiable){
                                                characteristicUuid3= characteristic.uuid
                                                Log.d("rec_byte","macAdress3 =  $macAddress3 UUID 3=  ${characteristicUuid3}")
                                            }
                                    }
                    }
                }, {

                })

    }



    private fun prepareConnectionObservable3(): Observable<RxBleConnection> =
            bleDevice3
                    .establishConnection(false)
                    .takeUntil(disconnectTriggerSubject3)
                    .compose(ReplayingShare.instance())


    private fun onConnectToggleClick3() {
        if (bleDevice3.isConnected) {
            triggerDisconnect3()
        } else {
            connectionObservable3
                    .flatMapSingle { it.discoverServices() }
                    .flatMapSingle { it.getCharacteristic(characteristicUuid3) }
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnSubscribe { connect.setText(R.string.connecting) }
                    .subscribe(
                            { characteristic ->
                                updateUI(characteristic)
                                Log.i("rec_byte", "Hey, connection has been established! to macAddress3 =  $macAddress3" )
                            },
                            { onConnectionFailure(it) },
                            { updateUI(null) }
                    )
                    .let { connectionDisposable3.add(it) }
        }
    }

    /**
    Client 4
     */

    private fun triggerDisconnect4() = disconnectTriggerSubject4.onNext(Unit)

    @SuppressLint("CheckResult")
    private fun onConnectToggleClickGetChar4() {
        Log.d("Logging","THIS IS FROM onConnecte Methos")
        bleDevice4.establishConnection(false)
                .flatMapSingle { it.discoverServices() }
                .take(1) // Disconnect automatically after discovery
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe {}
                .doFinally {}
                .subscribe({
                    it.bluetoothGattServices.flatMap {service ->
                        service.characteristics.map { characteristic ->
                            if (characteristic.isNotifiable){
                                characteristicUuid4= characteristic.uuid
                                Log.d("rec_byte","macAdress4 =  $macAddress4 UUID 4=  ${characteristicUuid4}")
                            }
                        }
                    }
                }, {})

    }


    private fun prepareConnectionObservable4(): Observable<RxBleConnection> =
            bleDevice4
                    .establishConnection(false)
                    .takeUntil(disconnectTriggerSubject4)
                    .compose(ReplayingShare.instance())


    private fun onConnectToggleClick4() {
        if (bleDevice4.isConnected) {
            triggerDisconnect4()
        } else {
            connectionObservable4
                    .flatMapSingle { it.discoverServices() }
                    .flatMapSingle { it.getCharacteristic(characteristicUuid4) }
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnSubscribe { connect.setText(R.string.connecting) }
                    .subscribe(
                            { characteristic ->
                                updateUI(characteristic)
                                Log.i("rec_byte", "Hey, connection has been established! to macAddress4 =   $macAddress4" )
                            },
                            { onConnectionFailure(it) },
                            { updateUI(null) }
                    )
                    .let { connectionDisposable4.add(it) }
        }
    }

    /**
    Client 5
     */

    private fun triggerDisconnect5() = disconnectTriggerSubject5.onNext(Unit)

    @SuppressLint("CheckResult")
    private fun onConnectToggleClickGetChar5() {
        Log.d("Logging","THIS IS FROM onConnecte Methos")
        bleDevice5.establishConnection(false)
                .flatMapSingle { it.discoverServices() }
                .take(1) // Disconnect automatically after discovery
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe {}
                .doFinally {}
                .subscribe({
                    it.bluetoothGattServices.flatMap {service ->
                        service.characteristics.map { characteristic ->
                            if (characteristic.isNotifiable){
                                characteristicUuid5= characteristic.uuid
                                Log.d("rec_byte","macAdress5 =  $macAddress5 UUID 5=  ${characteristicUuid5}")
                            }
                        }
                    }
                }, {})

    }


    private fun prepareConnectionObservable5(): Observable<RxBleConnection> =
            bleDevice5
                    .establishConnection(false)
                    .takeUntil(disconnectTriggerSubject5)
                    .compose(ReplayingShare.instance())


    private fun onConnectToggleClick5() {
        if (bleDevice5.isConnected) {
            triggerDisconnect5()
        } else {
            connectionObservable5
                    .flatMapSingle { it.discoverServices() }
                    .flatMapSingle { it.getCharacteristic(characteristicUuid5) }
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnSubscribe { connect.setText(R.string.connecting) }
                    .subscribe(
                            { characteristic ->
                                updateUI(characteristic)
                                Log.i("rec_byte", "Hey, connection has been established! to macAddress5=   $macAddress5" )
                            },
                            { onConnectionFailure(it) },
                            { updateUI(null) }
                    )
                    .let { connectionDisposable5.add(it) }
        }
    }





}
