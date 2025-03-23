package com.example.ble.domain.repository

import com.example.ble.domain.model.BleDevice
import kotlinx.coroutines.flow.Flow
import java.util.UUID


/**
 * Defines the public interface for BLE operations.
 *
 * Responsibilities include scanning for BLE devices, managing connections,
 * and performing GATT operations such as reading, writing, and enabling notifications.
 */
interface BleRepository {

    /**
     * A flow emitting the list of discovered BLE devices.
     */
    val scanResults: Flow<List<BleDevice>>

    /**
     * A flow emitting the currently connected BLE device.
     */
    val connectedDevice: Flow<BleDevice>

    /**
     * A flow emitting BLE notification events as pairs of characteristic UUID and updated value.
     */
    val notifications: Flow<Pair<UUID, ByteArray>>

    /**
     * Starts scanning for BLE devices.
     */
    fun startScan()

    /**
     * Stops scanning for BLE devices.
     */
    fun stopScan()

    /**
     * Initiates connection to the given BLE device.
     *
     * @param device The BLE device to connect.
     */
    suspend fun connectToDevice(device: BleDevice): Boolean

    /**
     * Disconnects from the currently connected BLE device and cleans up GATT resources.
     */
    fun disconnectDevice()

    /**
     * Reads the value of a characteristic from the connected device.
     */
    suspend fun readCharacteristic(serviceUuid: UUID, characteristicUuid: UUID): ByteArray?

    /**
     * Writes data to a characteristic on the connected device.
     */
    suspend fun writeCharacteristic(
        serviceUuid: UUID,
        characteristicUuid: UUID,
        data: ByteArray
    ): Boolean

    /**
     * Enables or disables notifications for a characteristic on the connected device.
     */
    suspend fun setCharacteristicNotification(
        serviceUuid: UUID,
        characteristicUuid: UUID,
        enable: Boolean
    ): Boolean
}