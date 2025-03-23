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

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BleDevice

        if (name != other.name) return false
        if (address != other.address) return false
        if (!manufacturerData.contentEquals(other.manufacturerData)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name?.hashCode() ?: 0
        result = 31 * result + address.hashCode()
        result = 31 * result + (manufacturerData?.contentHashCode() ?: 0)
        return result
    }
}