package com.example.ble.data


import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
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

    // Shared flow to emit notification events (characteristic UUID paired with its new value)
    private val _notificationFlow = MutableSharedFlow<Pair<UUID, ByteArray>>(extraBufferCapacity = 1)
    val notificationFlow = _notificationFlow.asSharedFlow()

    override fun onCharacteristicRead(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic,
        status: Int
    ) {
        readDeferred?.let { deferred ->
            if (status == BluetoothGatt.GATT_SUCCESS) {
                deferred.complete(characteristic.value)
            } else {
                deferred.completeExceptionally(Exception("Characteristic read failed with status: $status"))
            }
            readDeferred = null
        }
    }

    override fun onCharacteristicRead(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic,
        value: ByteArray,
        status: Int
    ) {
        readDeferred?.let { deferred ->
            if (status == BluetoothGatt.GATT_SUCCESS) {
                deferred.complete(value)
            } else {
                deferred.completeExceptionally(Exception("Characteristic read failed with status: $status"))
            }
            readDeferred = null
        }
    }

    override fun onCharacteristicWrite(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic,
        status: Int
    ) {
        writeDeferred?.let { deferred ->
            if (status == BluetoothGatt.GATT_SUCCESS) {
                deferred.complete(true)
            } else {
                deferred.completeExceptionally(Exception("Characteristic write failed with status: $status"))
            }
            writeDeferred = null
        }
    }

    override fun onDescriptorWrite(
        gatt: BluetoothGatt,
        descriptor: BluetoothGattDescriptor,
        status: Int
    ) {
        descriptorDeferred?.let { deferred ->
            if (status == BluetoothGatt.GATT_SUCCESS) {
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
    override fun onCharacteristicChanged(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic
    ) {
        // Emit the characteristic's UUID and new value to the shared flow.
        _notificationFlow.tryEmit(characteristic.uuid to characteristic.value)
    }

    /**
     * Called when a characteristic's value changes.
     *
     * This callback is invoked when notifications or indications are received.
     * The new value is emitted to [notificationFlow] so that subscribers can react.
     */
    override fun onCharacteristicChanged(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic,
        value: ByteArray
    ) {
        // Emit the characteristic's UUID and new value to the shared flow.
        _notificationFlow.tryEmit(characteristic.uuid to value)
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
}