package com.example.ble.domain.model

import android.bluetooth.BluetoothAdapter.STATE_DISCONNECTED
import android.bluetooth.BluetoothGatt

/**
 * Model representing a BLE device.
 *
 * @property name The name of the BLE device.
 * @property address The MAC address of the device.
 * @property manufacturerData Optional manufacturer-specific data from advertisements.
 * @property bluetoothGatt The BluetoothGatt instance for a connected device.
 * @property connectionState The current connection state (e.g. BluetoothGatt.STATE_CONNECTED, etc.).
 */
data class BleDevice(
    val name: String?,
    val address: String,
    val manufacturerData: ByteArray? = null,
    val bluetoothGatt: BluetoothGatt? = null,
    val connectionState: Int = STATE_DISCONNECTED,
) {

    companion object {
        // A default BleDevice instance to represent a disconnected or "none" state.
        val None = BleDevice(name = null, address = "", manufacturerData = null, bluetoothGatt = null, connectionState = STATE_DISCONNECTED)
    }

}