package com.example.ble.data

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothStatusCodes
import android.bluetooth.le.ScanCallback
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresPermission
import com.example.ble.domain.model.BleDevice
import com.example.ble.domain.repository.BleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Singleton
import java.util.UUID
import javax.inject.Inject

/**
 * Implementation of the BLE repository using injected BLE handlers.
 *
 * This class uses [BluetoothLeScannerHandler] for scanning BLE devices and
 * [BluetoothGattCallbackHandler] for managing GATT operations.
 */
@Singleton
class BleRepositoryImpl @Inject constructor(
    private val context: Context,
    private val scannerHandler: BluetoothLeScannerHandler,
    private val gattCallbackHandler: BluetoothGattCallbackHandler
) : BleRepository {
    private val bluetoothAdapter: BluetoothAdapter? =
        (context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter

    override val scanResults: Flow<List<BleDevice>> = scannerHandler.scanResults

    private val _connectedDevice = MutableStateFlow<BleDevice>(BleDevice.None)
    override val connectedDevice: Flow<BleDevice> = _connectedDevice

    override val notifications: Flow<Pair<UUID, ByteArray>> = gattCallbackHandler.notificationFlow

    /**
     * Starts BLE scanning by delegating to [BluetoothLeScannerHandler].
     */
    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    override fun startScan() {
        scannerHandler.startScan()
    }

    /**
     * Stops BLE scanning by delegating to [BluetoothLeScannerHandler].
     */
    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    override fun stopScan() {
        scannerHandler.stopScan()
    }

    /**
     * Initiates a connection to the given BLE device.
     * Delegates the GATT connection to the system, providing our gattCallbackHandler.
     */
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override fun connectToDevice(device: BleDevice) {
        bluetoothAdapter?.getRemoteDevice(device.address)
            ?.connectGatt(context, false, gattCallbackHandler)?.let { gatt ->
                _connectedDevice.value = (device.copy(bluetoothGatt = gatt, connectionState = BluetoothGatt.STATE_CONNECTING))
            }
    }

    /**
     * Disconnects from the connected BLE device and cleans up the GATT resources.
     */
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override fun disconnectDevice() {
        _connectedDevice.value.bluetoothGatt?.let { gatt ->
            gatt.disconnect()
            gatt.close()
        }
        _connectedDevice.value = BleDevice.None
    }

    /**
     * Reads a GATT characteristic from the connected device.
     *
     * @param serviceUuid UUID of the service containing the characteristic.
     * @param characteristicUuid UUID of the characteristic to read.
     * @return The value of the characteristic.
     */
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override suspend fun readCharacteristic(serviceUuid: UUID, characteristicUuid: UUID): ByteArray? {
        val gatt = _connectedDevice.value.bluetoothGatt ?: throw IllegalStateException("No connected device")
        val service = gatt.getService(serviceUuid) ?: throw IllegalArgumentException("Service not found")
        val characteristic = service.getCharacteristic(characteristicUuid)
            ?: throw IllegalArgumentException("Characteristic not found")
        if (!gatt.readCharacteristic(characteristic)) {
            throw Exception("Failed to initiate characteristic read")
        }
        return gattCallbackHandler.awaitCharacteristicRead()
    }


    /**
     * Writes data to a GATT characteristic.
     *
     * @param serviceUuid UUID of the service.
     * @param characteristicUuid UUID of the characteristic.
     * @param data Data to write.
     * @return True if the write operation succeeded.
     */
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override suspend fun writeCharacteristic(
        serviceUuid: UUID,
        characteristicUuid: UUID,
        data: ByteArray
    ): Boolean {
        val gatt = _connectedDevice.value.bluetoothGatt ?: throw IllegalStateException("No connected device")
        val service = gatt.getService(serviceUuid) ?: throw IllegalArgumentException("Service not found")
        val characteristic = service.getCharacteristic(characteristicUuid)
            ?: throw IllegalArgumentException("Characteristic not found")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (gatt.writeCharacteristic(characteristic, data,
                    BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT) != BluetoothStatusCodes.SUCCESS) {
                throw Exception("Failed to initiate characteristic write")
            }
        } else {
            characteristic.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT

            @Suppress("DEPRECATION")
            characteristic.value = data
            @Suppress("DEPRECATION")
            if (!gatt.writeCharacteristic(characteristic)) {
                throw Exception("Failed to initiate characteristic write")
            }
        }
        return gattCallbackHandler.awaitCharacteristicWrite()
    }


    /**
     * Enables or disables notifications for a specific GATT characteristic.
     *
     * @param serviceUuid UUID of the service.
     * @param characteristicUuid UUID of the characteristic.
     * @param enable True to enable notifications, false to disable.
     * @return True if the operation succeeded.
     */
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override suspend fun setCharacteristicNotification(
        serviceUuid: UUID,
        characteristicUuid: UUID,
        enable: Boolean
    ): Boolean {
        val gatt = _connectedDevice.value.bluetoothGatt ?: throw IllegalStateException("No connected device")
        val service = gatt.getService(serviceUuid) ?: throw IllegalArgumentException("Service not found")
        val characteristic = service.getCharacteristic(characteristicUuid)
            ?: throw IllegalArgumentException("Characteristic not found")
        if (!gatt.setCharacteristicNotification(characteristic, enable)) {
            throw Exception("Failed to set characteristic notification")
        }
        // Write to the descriptor to complete enabling notifications.
        val descriptor = characteristic.getDescriptor(UUID.fromString(CLIENT_CHARACTERISTIC_CONFIG))
            ?: throw IllegalArgumentException("Descriptor not found")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (gatt.writeDescriptor(descriptor,
                    BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE) != BluetoothStatusCodes.SUCCESS) {
                throw Exception("Failed to initiate descriptor write")
            }
        }else {
            @Suppress("DEPRECATION")
            descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
            @Suppress("DEPRECATION")
            if (!gatt.writeDescriptor(descriptor)) {
                throw Exception("Failed to initiate descriptor write")
            }
        }

        return gattCallbackHandler.awaitDescriptorWrite()
    }

    companion object {
        // Standard UUID for Client Characteristic Configuration descriptor.
        const val CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb"
    }


}