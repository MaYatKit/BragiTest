package com.example.ble.domain.usecase

import com.example.ble.domain.repository.BleRepository
import java.util.UUID
import javax.inject.Inject

/**
 * Use case to read a GATT characteristic from the connected BLE device.
 */
class ReadCharacteristicUseCase @Inject constructor(
    private val bleRepository: BleRepository
) {
    /**
     * Reads the value of the specified characteristic.
     *
     * @param serviceUuid UUID of the service containing the characteristic.
     * @param characteristicUuid UUID of the characteristic.
     * @return The value of the characteristic as a ByteArray.
     */
    suspend fun execute(serviceUuid: UUID, characteristicUuid: UUID): ByteArray? {
        return bleRepository.readCharacteristic(serviceUuid, characteristicUuid)
    }
}
