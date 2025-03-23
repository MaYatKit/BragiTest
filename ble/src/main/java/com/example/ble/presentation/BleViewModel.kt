package com.example.ble.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ble.domain.model.BleDevice
import com.example.ble.domain.repository.BleRepository
import com.example.ble.domain.usecase.ConnectToDeviceUseCase
import com.example.ble.domain.usecase.DisconnectToDeviceUseCase
import com.example.ble.domain.usecase.ReadCharacteristicUseCase
import com.example.ble.domain.usecase.SetNotificationUseCase
import com.example.ble.domain.usecase.StartScanUseCase
import com.example.ble.domain.usecase.StopScanUseCase
import com.example.ble.domain.usecase.WriteCharacteristicUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject


/**
 * BleViewModel exposes all BLE middleware use cases.
 *
 * It provides reactive StateFlows for scan results and the connection state, as well as functions for:
 * - Starting and stopping BLE scans.
 * - Connecting to a BLE device.
 * - Reading, writing, and setting notifications for GATT characteristics.
 */
@HiltViewModel
class BleViewModel @Inject constructor(
    private val startScanUseCase: StartScanUseCase,
    private val stopScanUseCase: StopScanUseCase,
    private val connectToDeviceUseCase: ConnectToDeviceUseCase,
    private val disconnectToDeviceUseCase: DisconnectToDeviceUseCase,
    private val readCharacteristicUseCase: ReadCharacteristicUseCase,
    private val writeCharacteristicUseCase: WriteCharacteristicUseCase,
    private val setNotificationUseCase: SetNotificationUseCase,
    bleRepository: BleRepository
) : ViewModel() {

    // Expose scanned devices as a StateFlow.
    val scanResults = bleRepository.scanResults

    // Expose the current connected device.
    val connectedDevice: StateFlow<BleDevice> = bleRepository.connectedDevice
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), BleDevice.None)

    // Expose notifications from the BLE middleware.
    // Each emission is a Pair of (characteristic UUID, updated value).
    val notifications = bleRepository.notifications

    /**
     * Starts the BLE scanning process.
     */
    fun startScan() {
        viewModelScope.launch {
            startScanUseCase.execute()
        }
    }

    /**
     * Stops the BLE scanning process.
     */
    fun stopScan() {
        viewModelScope.launch {
            stopScanUseCase.execute()
        }
    }

    /**
     * Initiates connection to the given BLE device.
     */
    fun connectToDevice(device: BleDevice, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val result = connectToDeviceUseCase.execute(device)
            onResult(result)
        }
    }


    /**
     * Disconnects from the connected BLE device.
     */
    fun disConnectToDevice() {
        viewModelScope.launch {
            disconnectToDeviceUseCase.execute()
        }
    }

    /**
     * Reads a GATT characteristic from the connected device.
     *
     * @param serviceUuid the UUID of the service containing the characteristic.
     * @param characteristicUuid the UUID of the characteristic to read.
     * @param onResult a callback that returns the read byte array or null if an error occurs.
     */
    fun readCharacteristic(
        serviceUuid: UUID,
        characteristicUuid: UUID,
        onResult: (ByteArray?) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val result = readCharacteristicUseCase.execute(serviceUuid, characteristicUuid)
                onResult(result)
            } catch (e: Exception) {
                e.printStackTrace()
                onResult(null)
            }
        }
    }

    /**
     * Writes data to a GATT characteristic of the connected device.
     *
     * @param serviceUuid the UUID of the service.
     * @param characteristicUuid the UUID of the characteristic.
     * @param data the data to write.
     * @param onResult a callback that returns true if the write succeeded, false otherwise.
     */
    fun writeCharacteristic(
        serviceUuid: UUID,
        characteristicUuid: UUID,
        data: ByteArray,
        onResult: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val success = writeCharacteristicUseCase.execute(serviceUuid, characteristicUuid, data)
                onResult(success)
            } catch (e: Exception) {
                e.printStackTrace()
                onResult(false)
            }
        }
    }

    /**
     * Enables or disables notifications for a GATT characteristic.
     *
     * @param serviceUuid the UUID of the service.
     * @param characteristicUuid the UUID of the characteristic.
     * @param enable true to enable notifications, false to disable.
     * @param onResult a callback that returns true if the operation succeeded.
     */
    fun setNotification(
        serviceUuid: UUID,
        characteristicUuid: UUID,
        enable: Boolean,
        onResult: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val success = setNotificationUseCase.execute(serviceUuid, characteristicUuid, enable)
                onResult(success)
            } catch (e: Exception) {
                e.printStackTrace()
                onResult(false)
            }
        }
    }
}