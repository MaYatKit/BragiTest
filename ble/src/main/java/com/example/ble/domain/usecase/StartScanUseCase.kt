package com.example.ble.domain.usecase

import com.example.ble.domain.model.BleDevice
import com.example.ble.domain.repository.BleRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case to start scanning for BLE devices.
 */
class StartScanUseCase @Inject constructor(
    private val bleRepository: BleRepository
) {
    /**
     * Initiates BLE scanning and returns a Flow of discovered BLE devices.
     */
    fun execute() {
        bleRepository.startScan()
    }
}