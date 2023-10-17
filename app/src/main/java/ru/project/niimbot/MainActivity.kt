package ru.project.niimbot

import android.bluetooth.BluetoothAdapter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat


class MainActivity : AppCompatActivity() {

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

    val bAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val tvName = findViewById<TextView>(R.id.nameTv)

        val tvMac = findViewById<TextView>(R.id.macAddressTv)

        val btn = findViewById<Button>(R.id.btnGet)

        btn.setOnClickListener {

            checkPermissions()

            if (bAdapter == null) {
                Toast.makeText(applicationContext, "Bluetooth Not Supported", Toast.LENGTH_SHORT).show()
            } else {

                val pairedDevices = bAdapter.bondedDevices
                if (pairedDevices.size > 0) {
                    for (device in pairedDevices) {

                        val deviceName = device.name

                        val macAddress = device.address

                        tvName.append("$deviceName\n")
                        tvMac.append("$macAddress\n")
                    }
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
        shouldShowRequestPermissionRationale(android.Manifest.permission.BLUETOOTH)
        shouldShowRequestPermissionRationale(android.Manifest.permission.BLUETOOTH_ADMIN)

        if (Build.VERSION.SDK_INT >= 31) {
            shouldShowRequestPermissionRationale(android.Manifest.permission.BLUETOOTH_CONNECT)
            shouldShowRequestPermissionRationale(android.Manifest.permission.BLUETOOTH_SCAN)
        }

        shouldShowRequestPermissionRationale(android.Manifest.permission.ACCESS_FINE_LOCATION)
        shouldShowRequestPermissionRationale(android.Manifest.permission.ACCESS_COARSE_LOCATION)
    }

    companion object {
        private val REQUEST_PERMISSIONS: Array<String> = buildList {
            if (Build.VERSION.SDK_INT <= 30) {
                add(android.Manifest.permission.BLUETOOTH)
                add(android.Manifest.permission.BLUETOOTH_ADMIN)
            }
            if (Build.VERSION.SDK_INT >= 31) {
                add(android.Manifest.permission.BLUETOOTH_CONNECT)
                add(android.Manifest.permission.BLUETOOTH_SCAN)
            }
            add(android.Manifest.permission.ACCESS_FINE_LOCATION)
            add(android.Manifest.permission.ACCESS_COARSE_LOCATION)

        }.toTypedArray()
    }
}