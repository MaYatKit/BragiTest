package com.example.ble.data

import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanRecord
import android.bluetooth.BluetoothDevice
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@RunWith(MockitoJUnitRunner::class)
class BluetoothLeScannerHandlerTest {

    @Mock
    lateinit var bluetoothLeScanner: BluetoothLeScanner

    @Mock
    lateinit var scanResult: ScanResult

    @Mock
    lateinit var scanRecord: ScanRecord

    @Mock
    lateinit var bluetoothDevice: BluetoothDevice

    private lateinit var scannerHandler: BluetoothLeScannerHandler



    @Before
    fun setUp() {
        // Initialize the handler with our mocked BluetoothLeScanner.
        scannerHandler = BluetoothLeScannerHandler(bluetoothLeScanner)
    }

    @Test
    fun `onScanResult updates scanResults correctly`() = runBlocking {
        // Setup the mocks for a scan result.
        whenever(scanResult.device).thenReturn(bluetoothDevice)
        whenever(bluetoothDevice.name).thenReturn("Test Device")
        whenever(bluetoothDevice.address).thenReturn("00:11:22:33:44:55")
        whenever(scanResult.scanRecord).thenReturn(scanRecord)


        // Start scanning: this registers the scan callback.
        scannerHandler.startScan()

        // Capture the ScanCallback that was passed to startScan().
        val callbackCaptor = argumentCaptor<ScanCallback>()
        verify(bluetoothLeScanner).startScan(callbackCaptor.capture())
        val callback = callbackCaptor.firstValue

        // Simulate a scan result callback.
        callback.onScanResult(ScanSettings.CALLBACK_TYPE_ALL_MATCHES, scanResult)

        // Verify that the StateFlow now contains one BLE device with the expected details.
        val results = scannerHandler.scanResults.value
        assertEquals("There should be one scanned device", 1, results.size)
        val bleDevice = results.first()
        assertEquals("Test Device", bleDevice.name)
        assertEquals("00:11:22:33:44:55", bleDevice.address)
        // Mockito mock SparseArray just an empty implementation
        // assertArrayEquals(expectedData, bleDevice.manufacturerData)
    }

    @Test
    fun `onScanFailed does not update scanResults`() = runBlocking {
        // Start scanning.
        scannerHandler.startScan()
        val callbackCaptor = argumentCaptor<ScanCallback>()
        verify(bluetoothLeScanner).startScan(callbackCaptor.capture())
        val callback = callbackCaptor.firstValue

        // Simulate a scan failure.
        callback.onScanFailed(ScanCallback.SCAN_FAILED_APPLICATION_REGISTRATION_FAILED)

        // Since our implementation does not update the state on failure, the scanResults should remain empty.
        assertTrue("Scan results should be empty on failure", scannerHandler.scanResults.value.isEmpty())
    }
}