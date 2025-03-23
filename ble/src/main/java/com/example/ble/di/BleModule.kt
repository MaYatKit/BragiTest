package com.example.ble.di

import android.bluetooth.BluetoothManager
import android.content.Context
import com.example.ble.data.BleRepositoryImpl
import com.example.ble.data.BluetoothGattCallbackHandler
import com.example.ble.data.BluetoothLeScannerHandler
import com.example.ble.domain.repository.BleRepository
import com.example.ble.domain.usecase.ConnectToDeviceUseCase
import com.example.ble.domain.usecase.ReadCharacteristicUseCase
import com.example.ble.domain.usecase.SetNotificationUseCase
import com.example.ble.domain.usecase.StartScanUseCase
import com.example.ble.domain.usecase.StopScanUseCase
import com.example.ble.domain.usecase.WriteCharacteristicUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object BleModule {

    @Provides
    @Singleton
    fun provideBluetoothGattCallbackHandler(): BluetoothGattCallbackHandler {
        return BluetoothGattCallbackHandler()
    }

    @Provides
    @Singleton
    fun provideBluetoothLeScannerHandler(@ApplicationContext context: Context): BluetoothLeScannerHandler {
        val bluetoothManager = context.getSystemService(BluetoothManager::class.java)
        val bluetoothAdapter = bluetoothManager.adapter
        return BluetoothLeScannerHandler(bluetoothAdapter?.bluetoothLeScanner)
    }

    @Provides
    @Singleton
    fun provideBleRepository(
        @ApplicationContext context: Context,
        scannerHandler: BluetoothLeScannerHandler,
        gattCallbackHandler: BluetoothGattCallbackHandler
    ): BleRepository {
        return BleRepositoryImpl(context, scannerHandler, gattCallbackHandler)
    }

    // Optionally, provide use cases for the BLE middleware.
    @Provides
    @Singleton
    fun provideStartScanUseCase(repository: BleRepository): StartScanUseCase {
        return StartScanUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideStopScanUseCase(repository: BleRepository): StopScanUseCase {
        return StopScanUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideConnectToDeviceUseCase(repository: BleRepository): ConnectToDeviceUseCase {
        return ConnectToDeviceUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideReadCharacteristicUseCase(repository: BleRepository): ReadCharacteristicUseCase {
        return ReadCharacteristicUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideWriteCharacteristicUseCase(repository: BleRepository): WriteCharacteristicUseCase {
        return WriteCharacteristicUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideSetNotificationUseCase(repository: BleRepository): SetNotificationUseCase {
        return SetNotificationUseCase(repository)
    }
}