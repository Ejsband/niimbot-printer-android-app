package ru.project.niimbot.ui

import android.Manifest
import android.annotation.SuppressLint
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
import ru.project.niimbot.NiibotApplication
import ru.project.niimbot.R


class MainActivity : AppCompatActivity() {

    private val launcher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { map ->
            if (map.values.all { it }) {
                Toast.makeText(this, "Все разрешения выданы", Toast.LENGTH_SHORT)
                    .show()
            } else {
                Toast.makeText(this, "Не все разрешения выданы", Toast.LENGTH_SHORT)
                    .show()
            }
        }

    private var bluetoothDeviceId: String? = null

    private var pngImageInBase64: String? = null
    private var imageWidth: Float? = null
    private var imageHeight: Float? = null
    private var imageOrientation: Int? = null

    private var isError = false
    private var isCancel = false
    private val pageCount = 1
    private var quantity = 1
    private val printMode = 1
    private val printMultiple = 8f
    private val printDensity = 3
    private val totalQuantity = pageCount * quantity
    val jsonList = ArrayList<String>()
    val infoList = ArrayList<String>()
    private val jsonInfo =
        "{\"printerImageProcessingInfo\": {\"orientation\": ${imageOrientation}, \"margin\": [0,0,0,0], \"printQuantity\": $quantity, \"horizontalOffset\": 0, \"verticalOffset\": 0, \"width\": ${imageWidth}, \"height\": ${imageHeight}, \"printMultiple\": $printMultiple, \"epc\": \"\"}}"

    private lateinit var printer: JCPrintApi

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

    private val bAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        getIntentData()


        val tvName = findViewById<TextView>(R.id.nameTv)

        val tvMac = findViewById<TextView>(R.id.macAddressTv)

        val btn = findViewById<Button>(R.id.btnGet)

        val deviceButton2 = findViewById<Button>(R.id.deviceButton2)

        btn.setOnClickListener {

            if (bAdapter == null) {
                Toast.makeText(applicationContext, "Bluetooth Not Supported", Toast.LENGTH_SHORT)
                    .show()
            } else {
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.BLUETOOTH_CONNECT
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    val pairedDevices = bAdapter.bondedDevices
                    if (pairedDevices.size > 0) {
                        for (device in pairedDevices) {
                            val deviceName = device.name
                            val macAddress = device.address
                            bluetoothDeviceId = device.address
                            tvName.append("$deviceName \n")
                            tvMac.append("$macAddress\n")
                        }
                    }
                } else {
                    checkPermissions()
                }
            }
        }

        deviceButton2.setOnClickListener {
            printer = JCPrintApi.getInstance(callback)
            printer.init(NiibotApplication().getNiibotApplicationInstance())
            printer.initImageProcessingDefault("", "")
            printer.openPrinterByAddress(bluetoothDeviceId)


            printPicture()
        }
    }

    private fun printPicture() {

        if (printer.isConnection != 0) {
            Toast.makeText(this, "Принтер не подключён!", Toast.LENGTH_SHORT).show()
            return
        }

        infoList.add(jsonInfo)
        printer.drawEmptyLabel(imageWidth!!, imageHeight!!, imageOrientation!!, "")
        printer.drawLabelImage(this.pngImageInBase64, 0F, 0F,
            imageWidth!!, imageHeight!!, 0, 1, 127F)
        val jsonByte: ByteArray = printer.generateLabelJson()
        val jsonStr = jsonByte.decodeToString()
        jsonList.add(jsonStr)
        printer.setTotalQuantityOfPrints(totalQuantity)
        printer.startPrintJob(printDensity, 1, printMode, object : PrintCallback {

            override fun onProgress(
                pageIndex: Int,
                quantityIndex: Int,
                hashMap: HashMap<String, Any>
            ) {
                if (pageIndex == pageCount && quantityIndex == quantity) {
                    if (printer.endJob()) {
                        Log.d("XXX", "Успешное завершение печати")
                    } else {
                        Log.d("XXX", "Не удалось завершить печать")
                    }
                }
            }

            override fun onError(i: Int) {}

            override fun onError(errorCode: Int, printState: Int) {
                isError = true
                var errorMessage = ""
                when (errorCode) {
                    1 -> errorMessage = "Открыта крышка"
                    2 -> errorMessage = "Нехватка бумаги"
                    3 -> errorMessage = "Низкий заряд батареи"
                    4 -> errorMessage = "Неисправная батарея"
                    5 -> errorMessage = "Ручная остановка печати"
                    6 -> errorMessage = "Ошибка данных"
                    7 -> errorMessage = "Принтер перегрелся"
                    8 -> errorMessage = "Ненормальный выход бумаги"
                    9 -> errorMessage = "Ошибка печати"
                    10 -> errorMessage = "Печатающая головка не обнаружена"
                    11 -> errorMessage = "Температура окружающей среды слишком низкая"
                    12 -> errorMessage = "Печатающая головка не заблокирована"
                    13 -> errorMessage = "Лента не обнаружена"
                    14 -> errorMessage = "Несоответствующая лента"
                    15 -> errorMessage = "Лента закончилась"
                    16 -> errorMessage = "Неподдерживаемые типы бумаги"
                    17 -> errorMessage = "Не удалось установить тип бумаги"
                    18 -> errorMessage = "Сбой настройки режима печати"
                    19 -> errorMessage = "Не удалось установить уровень концентрации тонера"
                    20 -> errorMessage = "Ошибка rfid записи"
                    21 -> errorMessage = "Не удалось настроить поля"
                    22 -> errorMessage = "Нарушение связи с принтером"
                    23 -> errorMessage = "Принтер отключен"
                    24 -> errorMessage = "Ошибка параметра чертежной доски"
                    25 -> errorMessage = "Неправильный угол поворота"
                    26 -> errorMessage = "ошибка json параметра"
                    27 -> errorMessage = "Ненормальный выход бумаги"
                    28 -> errorMessage = "Проверьте тип бумаги"
                    29 -> errorMessage = "RFID-метка не была записана"
                    30 -> errorMessage = "Установлен неверный уровень концентрации тонера"
                    31 -> errorMessage = "Неподдерживаемый режим печати"
                    else -> {}
                }
                Log.d("XXX", "Произошла ошибка: $errorMessage")
            }

            override fun onCancelJob(isSuccess: Boolean) {
                isCancel = true
            }

            override fun onBufferFree(pageIndex: Int, bufferSize: Int) {
                if (isError) {
                    return
                }
                if (pageIndex > pageCount) {
                    return
                }
                printer.commitData(jsonList, infoList)
            }
        }
        )
    }

    private fun getIntentData() {
        val pngImageInBase64 = intent.getStringExtra("pngImageInBase64")
        val imageWidth = intent.getStringExtra("imageWidth")
        val imageHeight = intent.getStringExtra("imageHeight")
        val imageOrientation = intent.getStringExtra("imageOrientation")

        if (pngImageInBase64 != null && imageWidth != null && imageHeight != null && imageOrientation != null) {
            this.pngImageInBase64 = pngImageInBase64
            this.imageWidth = imageWidth.toFloat()
            this.imageHeight = imageHeight.toFloat()
            this.imageOrientation = imageOrientation.toInt()
        } else {
            Toast.makeText(this, "Один или несколько параметров равны null", Toast.LENGTH_SHORT)
                .show()
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
            Toast.makeText(this, "Предоставлены все необходимые разрешения", Toast.LENGTH_SHORT)
                .show()
        } else {
            launcher.launch(REQUEST_PERMISSIONS)
        }

        if (Build.VERSION.SDK_INT <= 30) {
            shouldShowRequestPermissionRationale(Manifest.permission.BLUETOOTH)
            shouldShowRequestPermissionRationale(Manifest.permission.BLUETOOTH_ADMIN)
        }

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