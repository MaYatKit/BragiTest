package com.example.ble.data

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresPermission
import com.example.ble.domain.model.BleDevice
import com.example.ble.domain.repository.BleRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext

import java.util.UUID

class BleRepositoryImpl(private val context: Context) : BleRepository {
    private val bluetoothAdapter: BluetoothAdapter? =
        (context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter
    private val bluetoothLeScanner = bluetoothAdapter?.bluetoothLeScanner

    private val _scanResults = MutableStateFlow<List<BleDevice>>(emptyList())
    val scanResults: Flow<List<BleDevice>> = _scanResults

    private val _connectedDevice = MutableStateFlow<BleDevice>(BleDevice.None)
    val connectedDevice: Flow<BleDevice> = _connectedDevice

    private var scanCallback: ScanCallback? = null


    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    override fun startScan() {
        scanCallback = object : ScanCallback() {
                @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
                override fun onScanResult(callbackType: Int, result: ScanResult) {
                    val device = result.device
                    val manufacturerData = result.scanRecord?.manufacturerSpecificData?.valueAt(0)
                    val bleDevice = BleDevice(device.name, device.address, manufacturerData)
                    _scanResults.value = (_scanResults.value + bleDevice).distinctBy { it.address }
                }
            }
        bluetoothLeScanner?.startScan(scanCallback)
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    override fun stopScan() {
        scanCallback?.let {
            bluetoothLeScanner?.stopScan(it)
        }
        scanCallback = null
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override fun connectToDevice(device: BleDevice) {
        bluetoothAdapter?.getRemoteDevice(device.address)
            ?.connectGatt(context, false, object : BluetoothGattCallback() {
                @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
                override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
                    _connectedDevice.value = _connectedDevice.value.copy(
                        bluetoothGatt = gatt,
                        connectionState = newState
                    )
                }

                override fun onCharacteristicRead(
                    gatt: BluetoothGatt,
                    characteristic: BluetoothGattCharacteristic,
                    value: ByteArray,
                    status: Int
                ) {
                    super.onCharacteristicRead(gatt, characteristic, value, status)
                    _connectedDevice.value = _connectedDevice.value.copy(
                        messageReceived = value.decodeToString()
                    )
                }

                override fun onCharacteristicWrite(
                    gatt: BluetoothGatt?,
                    characteristic: BluetoothGattCharacteristic?,
                    status: Int
                ) {
                    super.onCharacteristicWrite(gatt, characteristic, status)
                    _connectedDevice.value = _connectedDevice.value.copy(
                        messageSent = status == BluetoothGatt.GATT_SUCCESS
                    )
                }

                override fun onCharacteristicChanged(
                    gatt: BluetoothGatt,
                    characteristic: BluetoothGattCharacteristic,
                    value: ByteArray
                ) {
                    super.onCharacteristicChanged(gatt, characteristic, value)
                    _connectedDevice.value = _connectedDevice.value.copy(
                        notificationReceived = value.decodeToString()
                    )
                }

            })
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override fun disconnectDevice(device: BleDevice) {
        device.bluetoothGatt?.disconnect()
        device.bluetoothGatt?.close()
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override suspend fun readCharacteristic(
        device: BleDevice,
        serviceUuid: UUID,
        characteristicUuid: UUID
    ) {
        val gatt = device.bluetoothGatt
        val characteristic = gatt?.getService(serviceUuid)?.getCharacteristic(characteristicUuid)

        withContext(Dispatchers.IO) {
            gatt?.readCharacteristic(characteristic)
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override suspend fun writeCharacteristic(
        device: BleDevice,
        serviceUuid: UUID,
        characteristicUuid: UUID,
        data: ByteArray
    ) {
        val gatt = device.bluetoothGatt
        val characteristic = gatt?.getService(serviceUuid)?.getCharacteristic(characteristicUuid)

        withContext(Dispatchers.IO) {
            if (characteristic != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    gatt.writeCharacteristic(characteristic, data, BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT)
                } else {
                    characteristic.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT

                    @Suppress("DEPRECATION")
                    characteristic.value = data
                    @Suppress("DEPRECATION")
                    gatt.writeCharacteristic(characteristic)
                }
            }
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override fun observeNotifications(
        device: BleDevice,
        serviceUuid: UUID,
        characteristicUuid: UUID
    ) {
        val gatt = device.bluetoothGatt
        val characteristic = gatt?.getService(serviceUuid)?.getCharacteristic(characteristicUuid)

        if (characteristic != null) {
            gatt.setCharacteristicNotification(characteristic, true)

            characteristic.descriptors.firstOrNull()?.let { descriptor ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    gatt.writeDescriptor(descriptor, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
                }else {
                    @Suppress("DEPRECATION")
                    descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                    @Suppress("DEPRECATION")
                    gatt.writeDescriptor(descriptor)
                }
            }
        }
    }


}