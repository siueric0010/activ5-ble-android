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
import android.util.Half.toFloat
import android.view.View
import android.view.Window
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_game.*
import kotlinx.android.synthetic.main.activity_main.*
import java.lang.Thread
import java.util.*
import kotlin.concurrent.thread

class GameActivity : AppCompatActivity(), A5BluetoothCallback {
    val MAX_SECONDS = 2
    private var max = 350
    private var sessionBest = 0
    private var seconds = MAX_SECONDS
    private var hasNotReachedZero = false

    private lateinit var audioManager: AudioManager

    /* Ensure media audio is paused, as we want to pause music */
    private val audioAttributes: AudioAttributes =
        AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA).build()

    /* Take focus temporarily. For the moment, we don't care if audio focus changes */
    private val audioFocusRequest: AudioFocusRequest =
        AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
            .setAudioAttributes(audioAttributes).setOnAudioFocusChangeListener { }.build()




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)
        val device = MainActivity.device
        backButton.visibility = View.INVISIBLE
        startBluetooth()
        if(device != null)
            A5DeviceManager.connect(this, device)
        device?.startIsometric()
        seconds = MAX_SECONDS
        sessionBest = 0

        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }
    override fun onResume() {
        super.onResume()
        seconds = MAX_SECONDS
        startBluetooth()
    }


    override fun bluetoothIsSwitchedOff() {
        Toast.makeText(this, "bluetooth is switched off", Toast.LENGTH_SHORT).show()
    }

    override fun searchCompleted() {
        Toast.makeText(this, "search completed", Toast.LENGTH_SHORT).show()
    }

    override fun didReceiveIsometric(device: A5Device, value: Int) {

        thread { // launch a new coroutine in background and continue
            Thread.sleep(1000L) // non-blocking delay for 1 second (default time unit is ms)
            seconds--
        }
        if(seconds == 0) {
            backButton.visibility = View.VISIBLE
        }
        gameScreenIsometric(device, value)
    }
    private fun gameScreenIsometric(thisDevice: A5Device, thisValue: Int) {
       /* if(thisValue > max) {
            max = thisValue
            progressBar2.max = max
        }*/
        //if(thisValue >= max / 2) {
        if(thisValue != 0  || hasNotReachedZero) {
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 3 +(thisValue / 30), AudioManager.FLAG_SHOW_UI)
            hasNotReachedZero = false
        } else if (thisValue > 0) {
            hasNotReachedZero = true
        }
        //} else if (thisValue < max / 2) {
          //  audioManager.adjustVolume(-1, AudioManager.FLAG_SHOW_UI)
        //}
        runOnUiThread {
            progressBar2.setProgress(100.times((thisValue.toFloat().div(max.toFloat()))).toInt(), true)
            textView.text = String.format("%d", 100.times((thisValue.toFloat().div(max.toFloat()))).toInt())
        }
    }


    override fun onWriteCompleted(device: A5Device, value: String) {
    }

    override fun deviceConnected(device: A5Device) {
    }

    override fun deviceFound(device: A5Device) {
    }

    override fun deviceDisconnected(device: A5Device) {
    }

    override fun on133Error() {
    }

    private fun startBluetooth() {
        val bluetoothManager = A5App().getInstance().getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val bluetoothAdapter = bluetoothManager.adapter

        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
            val enableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableIntent, MainActivity.Values.REQUEST_ENABLE_INTENT)
        } else {
            A5DeviceManager.setCallback(this)
            A5DeviceManager.scanForDevices()
        }
    }

}
