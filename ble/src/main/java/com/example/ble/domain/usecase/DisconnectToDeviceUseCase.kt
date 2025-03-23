package com.example.ble.domain.usecase

import com.example.ble.domain.repository.BleRepository
import javax.inject.Inject

/**
 * Use case to disconnect to a BLE device.
 */
class DisconnectToDeviceUseCase @Inject constructor(
    private val bleRepository: BleRepository
) {
    /**
     * Disconnect from the connected BLE device.
     */
    fun execute() {
        bleRepository.disconnectDevice()
    }
}