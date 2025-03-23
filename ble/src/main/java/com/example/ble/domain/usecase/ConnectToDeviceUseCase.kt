package com.example.ble.domain.usecase

import com.example.ble.domain.model.BleDevice
import com.example.ble.domain.repository.BleRepository
import javax.inject.Inject

/**
 * Use case to connect to a BLE device.
 */
class ConnectToDeviceUseCase @Inject constructor(
    private val bleRepository: BleRepository
) {
    /**
     * Initiates a connection to the specified BLE device.
     *
     * @param device The BLE device to connect to.
     */
    fun execute(device: BleDevice) {
        bleRepository.connectToDevice(device)
    }
}