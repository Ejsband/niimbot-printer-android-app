package ru.odinesina.niimbot.domain

import android.util.Log
import com.gengcon.www.jcprintersdk.JCPrintApi
import com.gengcon.www.jcprintersdk.callback.Callback
import ru.odinesina.niimbot.NiibotApplication

class PrinterUseCase {

    private val callback: Callback = object : Callback {
        override fun onConnectSuccess(s: String) {
            Log.d("XXX", "Подключение к принтеру.")
        }

        override fun onDisConnect() {
            Log.d("XXX", "Отключение от принтера")
        }

        override fun onElectricityChange(i: Int) {}
        override fun onCoverStatus(i: Int) {}
        override fun onPaperStatus(i: Int) {}
        override fun onRfidReadStatus(i: Int) {}
        override fun onPrinterIsFree(i: Int) {}
        override fun onHeartDisConnect() {}
        override fun onFirmErrors() {}
    }

    private lateinit var api: JCPrintApi

    fun getPrinter(bluetoothDeviceId: String): JCPrintApi {
        api = JCPrintApi.getInstance(callback)
        api.init(NiibotApplication().getNiibotApplicationInstance())
        api.initImageProcessingDefault("", "")
        api.openPrinterByAddress(bluetoothDeviceId)
        return api
    }

//    fun printImage(
//        pngImageInBase64: String,
//        imageWidth: Float,
//        imageHeight: Float,
//        imageOrientation: Int,
//        imageSettings: String
//    ) {
//
//    }
}