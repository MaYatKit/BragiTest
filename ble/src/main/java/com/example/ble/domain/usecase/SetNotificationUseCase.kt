package com.example.ble.domain.usecase

import com.example.ble.domain.repository.BleRepository
import java.util.UUID
import javax.inject.Inject


/**
 * Use case to enable or disable notifications for a specific characteristic.
 */
class SetNotificationUseCase @Inject constructor(
    private val bleRepository: BleRepository
) {
    /**
     * Enables or disables notifications for the specified characteristic.
     *
     * @param serviceUuid UUID of the service.
     * @param characteristicUuid UUID of the characteristic.
     * @param enable True to enable notifications, false to disable.
     * @return True if the operation succeeded.
     */
    suspend fun execute(serviceUuid: UUID, characteristicUuid: UUID, enable: Boolean): Boolean {
        return bleRepository.setCharacteristicNotification(serviceUuid, characteristicUuid, enable)
    }
}