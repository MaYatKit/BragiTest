package com.example.ble.domain.usecase

import com.example.ble.domain.repository.BleRepository
import javax.inject.Inject

/**
 * Use case to stop scanning for BLE devices.
 */
class StopScanUseCase @Inject constructor(
    private val bleRepository: BleRepository
) {
    /**
     * Stops BLE scanning.
     */
    fun execute() {
        bleRepository.stopScan()
    }
}
