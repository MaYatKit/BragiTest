package com.example.ble.domain.usecase

import com.example.ble.domain.repository.BleRepository
import java.util.UUID
import javax.inject.Inject

/**
 * Use case to write data to a GATT characteristic of the connected BLE device.
 */
class WriteCharacteristicUseCase @Inject constructor(
    private val bleRepository: BleRepository
) {
    /**
     * Writes the specified data to the characteristic.
     *
     * @param serviceUuid UUID of the service.
     * @param characteristicUuid UUID of the characteristic.
     * @param data The data to write.
     * @return True if the write operation succeeded.
     */
    suspend fun execute(serviceUuid: UUID, characteristicUuid: UUID, data: ByteArray): Boolean {
        return bleRepository.writeCharacteristic(serviceUuid, characteristicUuid, data)
    }
}