package com.example.ble.data


import android.bluetooth.BluetoothAdapter.STATE_CONNECTED
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.util.Log
import androidx.annotation.RequiresPermission
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import java.util.UUID

/**
 * BluetoothGattCallbackHandler for a single connected BLE device.
 *
 * This handler uses single deferred objects for each operation type,
 * assuming that operations occur sequentially.
 */
class BluetoothGattCallbackHandler : BluetoothGattCallback() {

    // Single deferred for each operation type.
    private var readDeferred: CompletableDeferred<ByteArray?>? = null
    private var writeDeferred: CompletableDeferred<Boolean>? = null
    private var descriptorDeferred: CompletableDeferred<Boolean>? = null
    private var connectedDeferred: CompletableDeferred<Boolean>? = null

    // Shared flow to emit notification events (characteristic UUID paired with its new value)
    private val _notificationFlow = MutableSharedFlow<Pair<UUID, ByteArray>>(extraBufferCapacity = 1)
    val notificationFlow = _notificationFlow.asSharedFlow()

    @RequiresPermission(android.Manifest.permission.BLUETOOTH_CONNECT)
    override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
        super.onConnectionStateChange(gatt, status, newState)
        if (newState == STATE_CONNECTED) {
            Log.d(TAG, "Connected to device, name: ${gatt?.device?.name}")
            connectedDeferred?.complete(true)
            connectedDeferred = null
            gatt?.discoverServices()
        }
    }

    @RequiresPermission(android.Manifest.permission.BLUETOOTH_CONNECT)
    override fun onCharacteristicRead(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic,
        status: Int
    ) {
        super.onCharacteristicRead(gatt, characteristic, status)
        readDeferred?.let { deferred ->
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "Characteristic Read, name : ${gatt.device.name}, characteristic value: ${characteristic.value.decodeToString()}")
                deferred.complete(characteristic.value)
            } else {
                deferred.completeExceptionally(Exception("Characteristic read failed with status: $status"))
            }
            readDeferred = null
        }
    }

    @RequiresPermission(android.Manifest.permission.BLUETOOTH_CONNECT)
    override fun onCharacteristicRead(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic,
        value: ByteArray,
        status: Int
    ) {
        super.onCharacteristicRead(gatt, characteristic, value, status)
        readDeferred?.let { deferred ->
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "Characteristic Read, name : ${gatt.device.name}, value: ${value.decodeToString()}")
                deferred.complete(value)
            } else {
                deferred.completeExceptionally(Exception("Characteristic read failed with status: $status"))
            }
            readDeferred = null
        }
    }

    @RequiresPermission(android.Manifest.permission.BLUETOOTH_CONNECT)
    override fun onCharacteristicWrite(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic,
        status: Int
    ) {
        super.onCharacteristicWrite(gatt, characteristic, status)
        writeDeferred?.let { deferred ->
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "Characteristic Write, name: ${gatt.device.name}")
                deferred.complete(true)
            } else {
                deferred.completeExceptionally(Exception("Characteristic write failed with status: $status"))
            }
            writeDeferred = null
        }
    }

    @RequiresPermission(android.Manifest.permission.BLUETOOTH_CONNECT)
    override fun onDescriptorWrite(
        gatt: BluetoothGatt,
        descriptor: BluetoothGattDescriptor,
        status: Int
    ) {
        super.onDescriptorWrite(gatt, descriptor, status)
        descriptorDeferred?.let { deferred ->
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "DescriptorWrite Write, name: ${gatt.device.name}")
                deferred.complete(true)
            } else {
                deferred.completeExceptionally(Exception("Descriptor write failed with status: $status"))
            }
            descriptorDeferred = null
        }
    }

    /**
     * Called when a characteristic's value changes.
     *
     * This callback is invoked when notifications or indications are received.
     * The new value is emitted to [notificationFlow] so that subscribers can react.
     */
    @RequiresPermission(android.Manifest.permission.BLUETOOTH_CONNECT)
    override fun onCharacteristicChanged(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic
    ) {
        super.onCharacteristicChanged(gatt, characteristic)
        Log.d(TAG, "Characteristic Changed Write, name: ${gatt.device.name}, uuid = ${characteristic.uuid}, characteristic value: ${characteristic.value.decodeToString()}")
        // Emit the characteristic's UUID and new value to the shared flow.
        _notificationFlow.tryEmit(characteristic.uuid to characteristic.value)
    }

    /**
     * Called when a characteristic's value changes.
     *
     * This callback is invoked when notifications or indications are received.
     * The new value is emitted to [notificationFlow] so that subscribers can react.
     */
    @RequiresPermission(android.Manifest.permission.BLUETOOTH_CONNECT)
    override fun onCharacteristicChanged(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic,
        value: ByteArray
    ) {
        super.onCharacteristicChanged(gatt, characteristic, value)
        Log.d(TAG, "Characteristic Changed Write, name: ${gatt.device.name}, uuid = ${characteristic.uuid}, value: ${value.decodeToString()}")
        // Emit the characteristic's UUID and new value to the shared flow.
        _notificationFlow.tryEmit(characteristic.uuid to value)
    }


    /**
     * Awaits the result of a connection operation.
     *
     * @return The result of the connection operation.
     */
    suspend fun awaitConnect(): Boolean {
        connectedDeferred = CompletableDeferred()
        return connectedDeferred!!.await()
    }

    /**
     * Awaits the result of a characteristic read operation.
     *
     * @return The value read from the characteristic.
     */
    suspend fun awaitCharacteristicRead(): ByteArray? {
        readDeferred = CompletableDeferred()
        return readDeferred!!.await()
    }

    /**
     * Awaits the result of a characteristic write operation.
     *
     * @return True if the write operation succeeded.
     */
    suspend fun awaitCharacteristicWrite(): Boolean {
        writeDeferred = CompletableDeferred()
        return writeDeferred!!.await()
    }

    /**
     * Awaits the result of a descriptor write operation.
     *
     * @return True if the descriptor write operation succeeded.
     */
    suspend fun awaitDescriptorWrite(): Boolean {
        descriptorDeferred = CompletableDeferred()
        return descriptorDeferred!!.await()
    }

    companion object {
        const val TAG = "BluetoothGattCallbackHandler"
    }
}