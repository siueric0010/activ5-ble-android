package com.example.bluetooth

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.CountDownTimer
import a5.com.a5bluetoothlibrary.A5DeviceManager
import a5.com.a5bluetoothlibrary.A5BluetoothCallback
import a5.com.a5bluetoothlibrary.A5Device
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.view.Window
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_game.*
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity(), A5BluetoothCallback {

    private var connectedDevices = mutableListOf<A5Device?>()
    companion object {
        public var device: A5Device? = null
    }
    private var counter: Int = 0
    private var countDownTimer: CountDownTimer? = null
    private var startTime =0
    private var endTime = 0
    private var toggled = false
    private var isPaused = false
    private var max = 100

    private lateinit var deviceAdapter: DeviceAdapter

    // Aidan's variables for audio
    private var paused = false

    private lateinit var audioManager: AudioManager

    /* Ensure media audio is paused, as we want to pause music */
    private val audioAttributes: AudioAttributes =
        AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA).build()

    /* Take focus temporarily. For the moment, we don't care if audio focus changes */
    private val audioFocusRequest: AudioFocusRequest =
        AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
            .setAudioAttributes(audioAttributes).setOnAudioFocusChangeListener { }.build()



    override fun bluetoothIsSwitchedOff() {
        Toast.makeText(this, "bluetooth is switched off", Toast.LENGTH_SHORT).show()
    }

    override fun searchCompleted() {
        Toast.makeText(this, "search completed", Toast.LENGTH_SHORT).show()
    }

    override fun didReceiveIsometric(device: A5Device, value: Int) {
        manageReceiveIsometric(device, value)
    }

    override fun onWriteCompleted(device: A5Device, value: String) {
    }

    override fun deviceConnected(device: A5Device) {
        device?.startIsometric()
    }

    override fun deviceFound(device: A5Device) {
        deviceAdapter.addDevice(device)
        connectedDevices.add(device)
    }

    override fun deviceDisconnected(device: A5Device) {
    }

    override fun on133Error() {
    }

    object Values {
        const val REQUEST_ENABLE_INTENT = 999
        const val MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 998
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.requestAudioFocus(audioFocusRequest)
//        button.setOnClickListener { changeButton() }

        requestPermission()
        initRecyclerView()

        connectButton.setOnClickListener {
            val device = MainActivity.device
            if (device != null) {
                A5DeviceManager.connect(this, device)
            }
        }

        disconnectButton.setOnClickListener {
            device?.disconnect()
        }

//        sendStopCommandButton.setOnClickListener {
//            device?.stop()
//            startTimer()
//        }
//
//        abortStopCommandButton.setOnClickListener {
//            device?.startIsometric()
//            stopTimer()
//        }
//
//        startIsometricButton.setOnClickListener {
//            //device?.startIsometric()
//        }
//
//        tareButton.setOnClickListener {
//            device?.tare()
//        }

        scanDevices.setOnClickListener {
            for (device in connectedDevices) {
                device?.disconnect()
            }
            device?.disconnect()
            device = null
            connectedDevices.clear()
            deviceAdapter.clearDevices()

            A5DeviceManager.scanForDevices()
        }
        gameStart.setOnClickListener {
            val intent = Intent(this, GameActivity::class.java)
            isPaused = true
            startActivity(intent)

        }
    }


    /*
     * https://medium.com/androiddevelopers/audio-focus-3-cdc09da9c122
     */
    private fun changeButton() {
        if (!paused) {
            audioManager.requestAudioFocus(audioFocusRequest)
            paused = true
        } else {
            audioManager.abandonAudioFocusRequest(audioFocusRequest)
            paused = false
        }
    }
    @Synchronized
    private fun print(name: String, value: Int) {
        runOnUiThread {
            pressureChangedTextView1.text =
                String.format(
                    Locale.US, "%s: %d", name, value
                )
        }
    }

//    @Synchronized
//    private fun print2(name: String, value: Int) {
//        runOnUiThread {
//            pressureChangedTextView2.text =
//                String.format(
//                    Locale.US, "%s: %d", name, value
//                )
//        }
//    }

    private fun manageReceiveIsometric(thisDevice: A5Device, thisValue: Int) {
        if(!isPaused) {
            mainScreenIsometric(thisDevice, thisValue)
        } else {
            gameScreenIsometric(thisDevice, thisValue)
        }
    }
    private fun gameScreenIsometric(thisDevice: A5Device, thisValue: Int) {
        if(thisValue > max) {
            max = thisValue
        }

        progressBar2.setProgress(100 * (thisValue / max), true)
        textView.setText(100 * (thisValue / max))

    }
    private fun mainScreenIsometric(thisDevice: A5Device, thisValue: Int) {

        if (connectedDevices.isNotEmpty()) {
            if (connectedDevices[0]?.device?.address == thisDevice.device.address) {
                print(thisDevice.device.name, thisValue)
            }
            /*else if (connectedDevices.size > 1 && connectedDevices[1]?.device?.address == thisDevice.device.address) {
                print2(thisDevice.device.name, thisValue)
            }*/
        }

        if(startTime - endTime > 10 && startTime - endTime < 350 && toggled) {
            changeButton()
            startTime = System.currentTimeMillis().toInt()
            endTime = System.currentTimeMillis().toInt()
            toggled = false
        }
        if(thisValue > 25) {
            startTime = System.currentTimeMillis().toInt()
        } else if (thisValue == 0) {
            toggled = true
            endTime = System.currentTimeMillis().toInt()
        }

    }
    override fun onResume() {
        super.onResume()
        isPaused = false
        startBluetooth()
    }

    fun deviceSelected(device: A5Device) {
        MainActivity.device = device
        Toast.makeText(this, "device selected: " + device.device.name, Toast.LENGTH_SHORT).show()
    }

    private fun initRecyclerView() {
        deviceAdapter = DeviceAdapter(this)

        val linearLayoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = linearLayoutManager
        recyclerView.adapter = deviceAdapter
    }

    private fun requestPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
                != PackageManager.PERMISSION_GRANTED
            ) {
                // Permission is not granted
                // Should we show an explanation?
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                ) {
                    // Show an explanation to the user *asynchronously* -- don't block
                    // this thread waiting for the user's response! After the user
                    // sees the explanation, try again to request the permission.
                } else {
                    // No explanation needed, we can request the permission.
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
                        Values.MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION
                    )

                    // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                    // app-defined int constant. The callback method gets the
                    // result of the request.
                }
            } else {
                startBluetooth()
            }
        } else {
            startBluetooth()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        when (requestCode) {
            Values.MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    startBluetooth()
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return
            }

            // Add other 'when' lines to check for other
            // permissions this app might request.
            else -> {
                // Ignore all other requests.
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == Values.REQUEST_ENABLE_INTENT) {
            if (resultCode == Activity.RESULT_OK) {
                startBluetooth()
            }
        }
    }

    private fun startBluetooth() {
        val bluetoothManager = A5App().getInstance().getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val bluetoothAdapter = bluetoothManager.adapter

        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
            val enableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableIntent, Values.REQUEST_ENABLE_INTENT)
        } else {
            A5DeviceManager.setCallback(this)
            A5DeviceManager.scanForDevices()
        }
    }

    private fun startTimer() {
        counter = 0
        countDownTimer = object : CountDownTimer(420000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                counter++
            }

            override fun onFinish() {
            }
        }.start()
    }

    private fun stopTimer() {
        countDownTimer?.cancel()
    }

}
