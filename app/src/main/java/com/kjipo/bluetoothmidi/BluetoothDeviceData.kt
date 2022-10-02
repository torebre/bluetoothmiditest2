package com.kjipo.bluetoothmidi

import android.bluetooth.BluetoothDevice

data class BluetoothDeviceData(val bluetoothDevice: BluetoothDevice) {

    override fun toString(): String {
        return "Name: ${bluetoothDevice.name}. Address: ${bluetoothDevice.address}"
    }

}