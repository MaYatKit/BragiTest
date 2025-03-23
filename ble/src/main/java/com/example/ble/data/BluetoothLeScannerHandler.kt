package com.example.ble.data

import android.Manifest
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.core.util.getOrElse
import androidx.core.util.isNotEmpty
import com.example.ble.domain.model.BleDevice
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Handler for BLE scanning operations.
 *
 * This handler wraps the Android BluetoothLeScanner and uses a [MutableStateFlow] to
 * emit discovered BLE devices (with their name and manufacturer data). It is designed for
 * a scenario with one connected BLE device, but scanning may discover multiple peripherals.
 */
class BluetoothLeScannerHandler(private val bluetoothLeScanner: BluetoothLeScanner?) {

    // StateFlow to hold discovered BLE devices.
    private val _scanResults = MutableStateFlow<List<BleDevice>>(emptyList())
    val scanResults: StateFlow<List<BleDevice>> get() = _scanResults

    private var scanCallback: ScanCallback? = null

    /**
     * Starts BLE scanning.
     *
     * Each discovered device is added to the state flow list (avoiding duplicates based on address).
     */
    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    fun startScan() {
        scanCallback = object : ScanCallback() {
            @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                super.onScanResult(callbackType, result)
                val device = result.device
                val data = result.scanRecord?.manufacturerSpecificData
                val manufacturerData = if (data != null && data.isNotEmpty()) data.valueAt(0) else null

                val bleDevice = BleDevice(device.name, device.address, manufacturerData)
                _scanResults.value = (_scanResults.value + bleDevice).distinctBy { it.address }
            }

            override fun onScanFailed(errorCode: Int) {
                super.onScanFailed(errorCode)
            }
        }
        bluetoothLeScanner?.startScan(scanCallback)
    }

    /**
     * Stops BLE scanning.
     */
    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    fun stopScan() {
        scanCallback?.let { bluetoothLeScanner?.stopScan(it) }
        scanCallback = null
    }

    companion object {
        const val TAG = "BluetoothLeScannerHandler"
    }
}