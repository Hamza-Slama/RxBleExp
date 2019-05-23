package com.connection.hamza.com.blerx

import android.app.Application
import com.polidea.rxandroidble2.LogConstants
import com.polidea.rxandroidble2.LogOptions
import com.polidea.rxandroidble2.RxBleClient

class SampleApplication : Application() {

    companion object {
        lateinit var rxBleClient: RxBleClient
            private set
//        lateinit var rxBleClient2: RxBleClient
//            private set
//        lateinit var rxBleClient3: RxBleClient
//            private set
    }

    override fun onCreate() {
        super.onCreate()
        rxBleClient = RxBleClient.create(this)
//        rxBleClient2 = RxBleClient.create(this)
//        rxBleClient3 = RxBleClient.create(this)
        RxBleClient.updateLogOptions(LogOptions.Builder()
                .setLogLevel(LogConstants.INFO)
                .setMacAddressLogSetting(LogConstants.MAC_ADDRESS_FULL)
                .setUuidsLogSetting(LogConstants.UUIDS_FULL)
                .setShouldLogAttributeValues(true)
                .build()
        )
    }
}
