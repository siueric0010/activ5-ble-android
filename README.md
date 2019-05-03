# activ5-sample-android-ble-library

This is Bluetooth low energy library for android

## Example

To run the example project, clone or download the repo.

## Requirements

Android Studio 3.4.0 or above

An Android device with BLE capabilities

Android SDK version 19 or above

## Installation

Open 'activ5-sample-android-ble-library'

Connect an Android device.

Build and run project.

## How to include it in your own project

1. Paste "a5bluetoothlibrary-release.aar" in the libs folder of your app
2. Add this code to dependencies in your app:gradle: implementation (name: 'a5bluetoothlibrary-release', ext:'aar')
3. Add this code in the same gradle: 
repositories {
    flatDir {
        dirs 'libs'
    }
}

# Use of framework

## Basic functionality

## Framework initialisation

In order to initialize the framework you need to call the following function. The best place to call it is in your Application class or somewhere before calling Bluetooth related functions.

```bash
A5DeviceManager.initializeDeviceManager()
```

You also need to set the callback, on which you will receive bluetooth events:
```bash
A5DeviceManager.setCallback(this)
```

## Search for devices

You need to search for devices in order to load the devices. Each time a new device has been found

```bash
A5DeviceManager.scanForDevices()
```

Once a device is found, the following callback is called
```bash
func deviceFound(device: A5Device) {
// Action when a device has been found
}
```

When the device search timeouts a delegate function is being called.
```bash
func searchCompleted() {
// Action when a device search has been completed (timed out)
}
```

## Connect to a device

Connecting a device is easy. You just need to call

```bash
A5DeviceManager.connect(context, device)
```

When a device has been connected the delegate function deviceConnected is going to be called.

```bash
func deviceConnected(device: A5Device) {
// Action to do when a device is connected. Probably ask for isometric data
}
```    
```bash
device.startIsometric()
```

The isometric data is going to be received in the function didReceiveIsometric(device: A5Device, value: Int). 
The value received is in Newtons and is returned on every 100 ms.

```bash
func didReceiveIsometric(device: A5Device, value: Int) {
// Action when isometric data is received
}
```    
## Stop receiving isometric data

In order to save device battery it is recommended to call stop() function. That way the device consumption drops to a minimum while still is being connected.

```bash
device.stop()
```    

NB: After 7 minutes in stop mode the device will switch switch off. If you don't want the device to timeout after 7 minutes you can switch on evergreen mode. This will keep the device awake.

```bash
device.evergreenMode = true
```    

If you want to include functionality to calibrate Activ5 you can use the Tare function. Call tare with the tare() method.

```bash
device.tare()
```   

## Disconnect a device

Disconnecting the device happens with calling disconnect() function

```bash
device.disconnect()
```    

After the device has been disconnected (it can happen also if the device is switched off by the user) the following delegate method is being called.

```bash
func deviceDisconnected(device: A5Device) {
// May show the user that the device has been disconnected or retry to connect if needed.
}
```    

# Extended documentation

# A5Device

## Functionality

Device Communication

```bash
func startIsometric()
func stop()
func tare()
func disconnect()
```

# A5DeviceManager

## Properties

```bash
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private var bluetoothGatt: BluetoothGatt? = null

    private var writeChar: BluetoothGattCharacteristic? = null
    private var a5BluetoothCallback: A5BluetoothCallback? = null

    private var a5Device: A5Device? = null

    private var scannedDevices: ArrayList<String> = arrayListOf()

    private var countDownTimer: CountDownTimer? = null

    private var isConnected: Boolean = false

    private var protocolVersion: String? = null

    private var currentPressure = 0
```


## Functionality

```bash
setCallback(A5BluetoothCallback)
scanForDevices()
```

## A5BluetoothCallback
```bash
    fun searchCompleted()

    fun deviceFound(device: A5Device)

    fun deviceConnected(device: A5Device)

    fun deviceDisconnected(device: A5Device)

    fun didReceiveMessage(device: A5Device, message: String)

    fun didReceiveIsometric(device: A5Device, value: Int)

    fun onWriteCompleted(device: A5Device, value: String)

    fun on133Error()

    fun bluetoothIsSwitchedOff()
```

# Author

Haris Kurbardovikj

# Licence

Activ5-sample-android-ble-library is available under the MIT license. See the LICENSE file for more info.
