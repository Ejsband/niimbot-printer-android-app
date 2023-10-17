package ru.project.niimbot

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.gengcon.www.jcprintersdk.JCPrintApi
import com.gengcon.www.jcprintersdk.callback.Callback
import com.gengcon.www.jcprintersdk.callback.PrintCallback
import java.util.HashMap


class MainActivity : AppCompatActivity() {

    private val mCallback = object : Callback {

        override fun onConnectSuccess(p0: String?) {
            Log.d("XXX", "Connection success")
        }

        override fun onDisConnect() {
            Log.d("XXX", "Disconnected from device")
        }

        override fun onElectricityChange(p0: Int) {
        }

        override fun onCoverStatus(p0: Int) {
        }

        override fun onPaperStatus(p0: Int) {
        }

        override fun onRfidReadStatus(p0: Int) {
        }

        override fun onPrinterIsFree(p0: Int) {
        }

        override fun onHeartDisConnect() {
        }

        override fun onFirmErrors() {
        }
    }

    private val printCallback = object : PrintCallback {
        override fun onProgress(p0: Int, p1: Int, p2: HashMap<String, Any>?) {

        }

        override fun onError(p0: Int) {

        }

        override fun onError(p0: Int, p1: Int) {

        }

        override fun onCancelJob(p0: Boolean) {

        }

        override fun onBufferFree(p0: Int, p1: Int) {

        }

    }

    private val launcher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { map ->
            if (map.values.all { it }) {
                Toast.makeText(this, "All permissions are granted", Toast.LENGTH_SHORT)
                    .show()
            } else {
                Toast.makeText(this, "Permissions are not granted", Toast.LENGTH_SHORT)
                    .show()
            }
        }

    private var jcapi: JCPrintApi = JCPrintApi.getInstance(mCallback)

    val bAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        jcapi.init(getApplication());
        jcapi.initImageProcessingDefault("", "")


        val tvName = findViewById<TextView>(R.id.nameTv)

        val tvMac = findViewById<TextView>(R.id.macAddressTv)

        val btn = findViewById<Button>(R.id.btnGet)

        btn.setOnClickListener {

            if (bAdapter == null) {
                Toast.makeText(applicationContext, "Bluetooth Not Supported", Toast.LENGTH_SHORT).show()
            } else {
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.BLUETOOTH_CONNECT
                    ) != PackageManager.PERMISSION_GRANTED) {
                    val pairedDevices = bAdapter.bondedDevices
                    if (pairedDevices.size > 0) {
                        for (device in pairedDevices) {
                            val deviceName = device.name
                            val macAddress = device.address
                            tvName.append("$deviceName \n")
                            tvMac.append("$macAddress\n")
                        }
                    }
                } else {
                    checkPermissions()
                }
            }
        }
    }

    private fun checkPermissions() {
        val isAllGranted = REQUEST_PERMISSIONS.all { permission ->
            ContextCompat.checkSelfPermission(
                this,
                permission
            ) == PackageManager.PERMISSION_GRANTED
        }
        if (isAllGranted) {
            Toast.makeText(this, "All permissions are granted!", Toast.LENGTH_SHORT)
                .show()
        } else {
            launcher.launch(REQUEST_PERMISSIONS)
        }
        shouldShowRequestPermissionRationale(Manifest.permission.BLUETOOTH)
        shouldShowRequestPermissionRationale(Manifest.permission.BLUETOOTH_ADMIN)

        if (Build.VERSION.SDK_INT >= 31) {
            shouldShowRequestPermissionRationale(Manifest.permission.BLUETOOTH_CONNECT)
            shouldShowRequestPermissionRationale(Manifest.permission.BLUETOOTH_SCAN)
        }

        shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)
        shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION)
    }

    companion object {
        private val REQUEST_PERMISSIONS: Array<String> = buildList {
            if (Build.VERSION.SDK_INT <= 30) {
                add(Manifest.permission.BLUETOOTH)
                add(Manifest.permission.BLUETOOTH_ADMIN)
            }
            if (Build.VERSION.SDK_INT >= 31) {
                add(Manifest.permission.BLUETOOTH_CONNECT)
                add(Manifest.permission.BLUETOOTH_SCAN)
            }
            add(Manifest.permission.ACCESS_FINE_LOCATION)
            add(Manifest.permission.ACCESS_COARSE_LOCATION)
        }.toTypedArray()
    }



}