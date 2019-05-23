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
import java.util.UUID




private const val EXTRA_MAC_ADDRESS = "extra_mac_address"

private const val EXTRA_CHARACTERISTIC_UUID = "extra_uuid"

class CharacteristicOperationExampleActivity : AppCompatActivity() {

    companion object {
        fun newInstance(context: Context, macAddress: String, uuid: UUID) =
            Intent(context, CharacteristicOperationExampleActivity::class.java).apply {
                putExtra(EXTRA_MAC_ADDRESS, macAddress)
                putExtra(EXTRA_CHARACTERISTIC_UUID, uuid)
            }
    }

    private lateinit var characteristicUuid: UUID

    private val disconnectTriggerSubject = PublishSubject.create<Unit>()

    private lateinit var connectionObservable: Observable<RxBleConnection>

    private val connectionDisposable = CompositeDisposable()

    private lateinit var bleDevice: RxBleDevice


    private val inputBytes: ByteArray
        get() = write_input.text.toString().toByteArray()


    /*
    Client 2
     */
    private lateinit var bleDevice2: RxBleDevice
    private lateinit var characteristicUuid2: UUID

    private val disconnectTriggerSubject2 = PublishSubject.create<Unit>()

    private lateinit var connectionObservable2: Observable<RxBleConnection>

    private val connectionDisposable2 = CompositeDisposable()
    //TODO : Add macAdress2 to connect
    private val macAddress2 = ""


    /**
     * Client 3
     */
    private lateinit var bleDevice3: RxBleDevice
    private lateinit var characteristicUuid3: UUID

    private val disconnectTriggerSubject3 = PublishSubject.create<Unit>()

    private lateinit var connectionObservable3: Observable<RxBleConnection>

    private val connectionDisposable3 = CompositeDisposable()
    //TODO : Add macAdress3 to connect
    private val macAddress3 = ""


    /**
     * Client 4
     */

    private lateinit var bleDevice4: RxBleDevice
    private lateinit var characteristicUuid4: UUID

    private val disconnectTriggerSubject4 = PublishSubject.create<Unit>()

    private lateinit var connectionObservable4: Observable<RxBleConnection>

    private val connectionDisposable4 = CompositeDisposable()
    //TODO : Add macAdress4 to connect
    private val macAddress4 = ""

    /**
     * Client 5
     */

    private lateinit var bleDevice5: RxBleDevice
    private lateinit var characteristicUuid5: UUID

    private val disconnectTriggerSubject5 = PublishSubject.create<Unit>()

    private lateinit var connectionObservable5: Observable<RxBleConnection>

    private val connectionDisposable5 = CompositeDisposable()
    //TODO : Add macAdress5 to connect
    private val macAddress5 = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_save)

        val macAddress = intent.getStringExtra(EXTRA_MAC_ADDRESS)
        characteristicUuid = intent.getSerializableExtra(EXTRA_CHARACTERISTIC_UUID) as UUID
        Log.d("TAG","UUID = $characteristicUuid")
        bleDevice = SampleApplication.rxBleClient.getBleDevice(macAddress)
        connectionObservable = prepareConnectionObservable()
        supportActionBar!!.subtitle = getString(R.string.mac_address, macAddress)


        bleDevice2 = SampleApplication.rxBleClient.getBleDevice(macAddress2)
        onConnectToggleClickGetChar()
        connectionObservable2 = prepareConnectionObservable2()

        bleDevice3 = SampleApplication.rxBleClient.getBleDevice(macAddress3)
        onConnectToggleClickGetChar3()
        connectionObservable3 = prepareConnectionObservable3()


        bleDevice4 = SampleApplication.rxBleClient.getBleDevice(macAddress4)
        onConnectToggleClickGetChar4()
        connectionObservable4 = prepareConnectionObservable4()

        bleDevice5 = SampleApplication.rxBleClient.getBleDevice(macAddress5)
        onConnectToggleClickGetChar5()
        connectionObservable5 = prepareConnectionObservable5()

        connect.setOnClickListener {
            onConnectToggleClick()
            onConnectToggleClick2()
            onConnectToggleClick3()
            onConnectToggleClick4()
            onConnectToggleClick5()
        }
        read.setOnClickListener { onReadClick() }
        write.setOnClickListener { onWriteClick() }
        notify.setOnClickListener {
            onNotifyClick()
            onNotifyClick2()
            onNotifyClick3()
            onNotifyClick4()
            onNotifyClick5()
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

    private fun onReadClick() {
        if (bleDevice.isConnected) {
            connectionObservable
                .firstOrError()
                .flatMap { it.readCharacteristic(characteristicUuid) }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ bytes ->
                    read_output.text = String(bytes)
                    read_hex_output.text = bytes?.toHex()
                    write_input.setText(bytes?.toHex())
                }, { onReadFailure(it) })
                .let { connectionDisposable.add(it) }
        }
    }

    private fun onWriteClick() {
        if (bleDevice.isConnected) {
            connectionObservable
                .firstOrError()
                .flatMap { it.writeCharacteristic(characteristicUuid, inputBytes) }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ onWriteSuccess() }, { onWriteFailure(it) })
                .let { connectionDisposable.add(it) }
        }
    }

    private fun onNotifyClick() {
        if (bleDevice.isConnected) {
            connectionObservable
                .flatMap {
                    it.setupNotification(characteristicUuid) }
                .doOnNext { runOnUiThread { notificationHasBeenSetUp() } }
                // we have to flatmap in order to get the actual notification observable
                // out of the enclosing observable, which only performed notification setup
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
                // we have to flatmap in order to get the actual notification observable
                // out of the enclosing observable, which only performed notification setup
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
                    // we have to flatmap in order to get the actual notification observable
                    // out of the enclosing observable, which only performed notification setup
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
                    // we have to flatmap in order to get the actual notification observable
                    // out of the enclosing observable, which only performed notification setup
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
                    // we have to flatmap in order to get the actual notification observable
                    // out of the enclosing observable, which only performed notification setup
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
        //showSnackbarShort("Connection error: $throwable")
        updateUI(null)
    }

    private fun onReadFailure(throwable: Throwable){
      //  showSnackbarShort("Read error: $throwable")
    }

    private fun onWriteSuccess() {
//        showSnackbarShort("Write success")
    }

    private fun onWriteFailure(throwable: Throwable) {
        //showSnackbarShort("Write error: $throwable")}
    }
    private fun onNotificationReceived(bytes: ByteArray,name : String){
        Log.d("rec_byte"," $name , ${bytes.toHex()}")
        Log.d("rec_byte"," $name , ${bytes.size}")
        if (bytes.size == 20 ) {
            setUp20Paq(bytes)
        }
       // showSnackbarShort("Change: ${bytes.toHex()}")
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
            read.isEnabled = false
            write.isEnabled = false
            notify.isEnabled = false
        } else {
            connect.setText(R.string.button_disconnect)
            with(characteristic) {
                read.isEnabled = hasProperty(BluetoothGattCharacteristic.PROPERTY_READ)
                write.isEnabled = hasProperty(BluetoothGattCharacteristic.PROPERTY_WRITE)
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
                                //startActivity(CharacteristicOperationExampleActivity.newInstance(this, macAddress2, characteristic.uuid))
                                //finish()
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



    /**
     * Common
     */

    fun setUp20Paq(data : ByteArray) {
        var nbrPaquet = 20
        val time1: Int
        val deltaTimes = IntArray(6)
        val val1 = IntArray(6)
        val val2 = IntArray(6)

        time1 = data[0].toInt() and 0xFF shl 12 or (data[1].toInt() and 0xFF shl 4) or (data[2].toInt() and 0xF0 shr 4)
        deltaTimes[0] = time1
        var iPaq = 2
        for (i in 0..5) {
            // 4 bits
            if (i > 0) deltaTimes[i] = (data[iPaq].toInt() and 0xFF shr 4) + deltaTimes[i - 1]
            // 10 bits = 4 bits (paq[i]) + 6 bits (paq[i+1])
            val1[i] = data[iPaq].toInt() and 0xF shl 6 or (data[iPaq + 1].toInt() and 0xFC shr 2)
            //2 bits (paq[i+1]) + 8 bits (paq[i+2])
            val2[i] = data[iPaq + 1].toInt() and 0x3 shl 8 or (data[iPaq + 2].toInt() and 0xFF)
            iPaq += 3
            // Log.d("Enregistrement_bleTAGE","deltaTimes ["+i+"] = "+deltaTimes[i]);

        }

        for (i in 0..5) {

            Log.d("Enregistrement_bleTAGE","deltatimes ["+i+"] = "+deltaTimes[i] )
            Log.d("Enregistrement_bleTAGE","val1 ["+i+"] = "+val1[i] )
            Log.d("Enregistrement_bleTAGE","val2 ["+i+"] = "+val2[i] )
        }
        intent.putExtra("deltaTimes", deltaTimes)
        intent.putExtra("val1", val1)
        intent.putExtra("val2", val2)
    }

}
