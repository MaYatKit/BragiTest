package com.example.ble.domain.repository

import com.example.ble.domain.model.BleDevice
import java.util.UUID

interface BleRepository {
    fun startScan()
    fun stopScan()
    fun connectToDevice(device: BleDevice)
    fun disconnectDevice(device: BleDevice)
    suspend fun readCharacteristic(device: BleDevice, serviceUuid: UUID, characteristicUuid: UUID)
    suspend fun writeCharacteristic(device: BleDevice, serviceUuid: UUID, characteristicUuid: UUID, data: ByteArray)
    fun observeNotifications(device: BleDevice, serviceUuid: UUID, characteristicUuid: UUID)
}