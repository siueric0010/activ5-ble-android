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
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_game.*
import kotlinx.android.synthetic.main.activity_main.*
import java.lang.Thread.sleep
import java.util.*

class GameActivity : AppCompatActivity(), A5BluetoothCallback {
    private var max = 350
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)
        val device = MainActivity.device
        startBluetooth()
        if(device != null)
            A5DeviceManager.connect(this, device)
        device?.startIsometric()
    }


    override fun bluetoothIsSwitchedOff() {
        Toast.makeText(this, "bluetooth is switched off", Toast.LENGTH_SHORT).show()
    }

    override fun searchCompleted() {
        Toast.makeText(this, "search completed", Toast.LENGTH_SHORT).show()
    }

    override fun didReceiveIsometric(device: A5Device, value: Int) {
        gameScreenIsometric(device, value)
    }
    private fun gameScreenIsometric(thisDevice: A5Device, thisValue: Int) {
        if(thisValue > max) {
            max = thisValue
            progressBar2.max = max
        } else {
            runOnUiThread {
                sleep(1000)
                if(progressBar2.max > 100) {
                    progressBar2.max -= progressBar2.max / 10
                } else {
                    progressBar2.max = 100
                }
            }

            progressBar2.max /= 10
        }


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
