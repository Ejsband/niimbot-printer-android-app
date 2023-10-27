package ru.project.niimbot.ui

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.gengcon.www.jcprintersdk.JCPrintApi
import com.gengcon.www.jcprintersdk.callback.Callback
import com.gengcon.www.jcprintersdk.callback.PrintCallback
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.commons.io.IOUtils
import ru.project.niimbot.NiibotApplication
import ru.project.niimbot.R
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.util.Base64


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

    var isError = false
    var isCancel = false
    val pageCount = 1
    var quantity = 1
    private val printMode = 1
    private val printMultiple = 8f
    private var width = 75f
    private var height = 120f
    private val printDensity = 3
    private val orientation = 0
    private val totalQuantity = pageCount * quantity
    val jsonList = ArrayList<String>()
    val infoList = ArrayList<String>()
    private val jsonInfo =
        "{\"printerImageProcessingInfo\": {\"orientation\": $orientation, \"margin\": [0,0,0,0], \"printQuantity\": $quantity, \"horizontalOffset\": 0, \"verticalOffset\": 0, \"width\": $width, \"height\": $height, \"printMultiple\": $printMultiple, \"epc\": \"\"}}"

    private lateinit var printer: JCPrintApi

    val callback: Callback = object : Callback {
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


    private var intentCode: String? = null
    private var address: String? = null
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
                            address = device.address
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
            printer.openPrinterByAddress(address)
            printPicture()

//            checkPermissions()

//            openFile("123.png")
        }
    }

    private fun printPicture() {

        if (printer.isConnection != 0) {
            Toast.makeText(this, "Принтер не подключён!", Toast.LENGTH_SHORT).show()
            return
        }


        infoList.add(jsonInfo)
        printer.drawEmptyLabel(width, height, orientation, "")
        val imageData: String = getJson(this, "image.json").replace("\"", "")
        printer.drawLabelImage(imageData, 0F, 0F, width, height, 0, 1, 127F)
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

    private fun getJson(context: Context, fileName: String): String {
        val stringBuilder = StringBuilder()
        try {
            val assetManager = context.assets
            val bf = BufferedReader(
                InputStreamReader(
                    assetManager.open(fileName)
                )
            )
            var line: String?
            while (bf.readLine().also { line = it } != null) {
                stringBuilder.append(line)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
//        return stringBuilder.toString()
//        return decodeImageToBase64(assets.open("111.png"))

        return intentCode!!

    }


    @SuppressLint("SdCardPath")
    private fun openFile(name: String) {
//        val file = File("/storage/emulated/0/Download/", name)
//        val file = File("/sdcard/DCIM/Camera/IMG_20231026_024056.jpg")
//
//        this.lifecycleScope.launch {
//
//            val inputAsString = withContext(Dispatchers.IO) {
//                IOUtils.toByteArray(FileInputStream(file))
//            }
//            Log.d("XXX", Base64.getEncoder().encodeToString(inputAsString))
//        }

//        val viewImageIntent = Intent(Intent.ACTION_VIEW)
//        viewImageIntent.setDataAndType(Uri.parse("content://media/external/images/media/1000000019/2023-10-26-03-11-40"), "image/jpg")
//        startActivity(viewImageIntent)
    }

//    private fun saveImageToInternalStorage(
//        context: Context,
//        bitmapFutureTarget: FutureTarget<Bitmap>,
//        fileName: String
//    ) {
//
//        this.lifecycleScope.launch {
//
//            val bitmap: Bitmap = withContext(Dispatchers.IO) {
//                bitmapFutureTarget.get()
//            }
//
//            val directory = File("/storage/emulated/0/Pictures")
//
//            if (!directory.exists()) {
//                directory.mkdirs()
//            }
//
//            val file = File(directory, fileName)
//
//            try {
//                withContext(Dispatchers.IO) {
//                    val outputStream = FileOutputStream(file)
//                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
//                    outputStream.flush()
//                    outputStream.close()
//                }
//
//                Toast.makeText(
//                    context,
//                    "File $fileName was saved!",
//                    Toast.LENGTH_LONG
//                ).show()
//
//            } catch (e: IOException) {
//                Toast.makeText(
//                    context,
//                    "Unable to save the file!",
//                    Toast.LENGTH_LONG
//                ).show()
//            }
//        }
//    }

    private fun decodeImageToBase64(imageInputStream: InputStream): String {
        return Base64.getEncoder().encodeToString(IOUtils.toByteArray(imageInputStream))
    }

    private fun getIntentData() {
        val intentData = intent.getStringExtra("url")

        if (intentData != null) {
            intentCode = intentData
//            Toast.makeText(this, intentData, Toast.LENGTH_SHORT).show()
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

        if (Build.VERSION.SDK_INT <= 28) {
            shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)
            shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)
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
            if (Build.VERSION.SDK_INT <= 28) {
                add(Manifest.permission.READ_EXTERNAL_STORAGE)
                add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }

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