package com.example.ble.domain.model

import android.bluetooth.BluetoothGatt

data class BleDevice(
    val name: String,
    val address: String,
    val manufacturerData: ByteArray?,
    val bluetoothGatt: BluetoothGatt? = null,
    val connectionState: Int? = -1,
    val messageSent: Boolean = false,
    val messageReceived: String = "",
    val notificationReceived: String = "",
) {

    companion object {
        val None = BleDevice("", "", null)
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