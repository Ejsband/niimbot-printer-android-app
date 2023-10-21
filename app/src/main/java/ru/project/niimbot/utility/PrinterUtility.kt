package ru.project.niimbot.utility

import android.util.Log
import com.gengcon.www.jcprintersdk.JCPrintApi
import com.gengcon.www.jcprintersdk.callback.Callback
import ru.project.niimbot.NiibotApplication

class PrinterUtility {

//    private val callback: Callback = object : Callback {
//        override fun onConnectSuccess(s: String) {
//            Log.d("XXX", "Подключение к принтеру.")
//        }
//        override fun onDisConnect() {
//            Log.d("XXX", "Отключение от принтера")
//        }
//        override fun onElectricityChange(i: Int) {}
//        override fun onCoverStatus(i: Int) {}
//        override fun onPaperStatus(i: Int) {}
//        override fun onRfidReadStatus(i: Int) {}
//        override fun onPrinterIsFree(i: Int) {}
//        override fun onHeartDisConnect() {}
//        override fun onFirmErrors() {}
//    }
//
//    private lateinit var api: JCPrintApi
//
//    fun getPrinterInstance(): JCPrintApi? {
//        api = JCPrintApi.getInstance(callback)
//        api.init(NiibotApplication().getNiibotApplicationInstance())
//        api.initImageProcessingDefault("", "")
//        return api
//    }
//
//    fun connectToPrinter(address: String?): Int {
//        getPrinterInstance()
//        return api.openPrinterByAddress(address)
//    }
//
//    fun closeConnection() {
//        getPrinterInstance()
//        api.close()
//    }
//
//    fun isConnected(): Int {
//        getPrinterInstance()
//        return api.isConnection
//    }
}