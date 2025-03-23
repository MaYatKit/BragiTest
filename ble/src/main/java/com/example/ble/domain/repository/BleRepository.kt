package com.example.ble.domain.repository

import com.example.ble.domain.model.BleDevice
import kotlinx.coroutines.flow.Flow
import java.util.UUID

interface BleRepository {

    val scanResults: Flow<List<BleDevice>>
    val connectedDevice: Flow<BleDevice>
    val notifications: Flow<Pair<UUID, ByteArray>>

    fun startScan()
    fun stopScan()
    fun connectToDevice(device: BleDevice)
    fun disconnectDevice()
    suspend fun readCharacteristic(serviceUuid: UUID, characteristicUuid: UUID): ByteArray?
    suspend fun writeCharacteristic(
        serviceUuid: UUID,
        characteristicUuid: UUID,
        data: ByteArray
    ): Boolean

    suspend fun setCharacteristicNotification(
        serviceUuid: UUID,
        characteristicUuid: UUID,
        enable: Boolean
    ): Boolean
}