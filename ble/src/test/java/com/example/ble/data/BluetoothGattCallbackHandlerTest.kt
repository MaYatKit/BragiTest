package com.example.ble.data

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.whenever
import java.util.UUID
import kotlin.test.assertFailsWith

@RunWith(MockitoJUnitRunner::class)
class BluetoothGattCallbackHandlerTest {

    @Mock
    lateinit var mockGatt: BluetoothGatt

    @Mock
    lateinit var mockCharacteristic: BluetoothGattCharacteristic

    @Mock
    lateinit var mockDescriptor: BluetoothGattDescriptor

    // The callback handler we want to test.
    private lateinit var callbackHandler: BluetoothGattCallbackHandler

    // A test UUID and expected data for notifications.
    private val testUuid: UUID = UUID.randomUUID()
    private val expectedNotificationData = byteArrayOf(0x0A, 0x0B, 0x0C)


    @Before
    fun setUp() {
        callbackHandler = BluetoothGattCallbackHandler()
    }

    @Test
    fun `onConnectionStateChange does not throw`() {
        // For this test, simply call onConnectionStateChange with common states.
        try {
            callbackHandler.onConnectionStateChange(mockGatt, 0, BluetoothGatt.STATE_CONNECTED)
            callbackHandler.onConnectionStateChange(mockGatt, 0, BluetoothGatt.STATE_DISCONNECTED)
            callbackHandler.onConnectionStateChange(mockGatt, 0, BluetoothGatt.STATE_DISCONNECTING)
            callbackHandler.onConnectionStateChange(mockGatt, 0, BluetoothGatt.STATE_CONNECTING)
        } catch (e: Exception) {
            fail("onConnectionStateChange should not throw an exception")
        }
    }


    @Test
    fun `awaitCharacteristicWrite returns true on success`() = runBlocking {
        // Launch a coroutine that awaits the write operation.
        val deferred = async { callbackHandler.awaitCharacteristicWrite() }
        // Allow the coroutine to start.
        delay(500)
        // Simulate a successful characteristic write.
        callbackHandler.onCharacteristicWrite(mockGatt, mockCharacteristic, BluetoothGatt.GATT_SUCCESS)
        val result = deferred.await()
        assertTrue("Characteristic write should return true on success", result)
    }

    @Test(expected = Exception::class)
    fun `awaitCharacteristicWrite throws exception on failure`() = runBlocking {
        val deferred = async { callbackHandler.awaitCharacteristicWrite() }
        delay(500)
        // Simulate a failure in characteristic write.
        callbackHandler.onCharacteristicWrite(mockGatt, mockCharacteristic, BluetoothGatt.GATT_FAILURE)
        val exception = assertFailsWith<Exception> {
            deferred.await()
        }
        assertTrue(exception.message?.contains("Characteristic write failed") == true)
    }

    @Test
    fun `awaitDescriptorWrite returns true on success`() = runBlocking {
        // Launch a coroutine that awaits the descriptor write operation.
        val deferred = async { callbackHandler.awaitDescriptorWrite() }
        delay(500)
        // Simulate a successful descriptor write.
        callbackHandler.onDescriptorWrite(mockGatt, mockDescriptor, BluetoothGatt.GATT_SUCCESS)
        val result = deferred.await()
        assertTrue("Descriptor write should return true on success", result)
    }

    @Test(expected = Exception::class)
    fun `awaitDescriptorWrite throws exception on failure`() = runBlocking {
        val deferred = async { callbackHandler.awaitDescriptorWrite() }
        delay(500)
        // Simulate a failure in descriptor write.
        callbackHandler.onDescriptorWrite(mockGatt, mockDescriptor, BluetoothGatt.GATT_FAILURE)
        val exception = assertFailsWith<Exception> {
            deferred.await()
        }
        assertTrue(exception.message?.contains("Descriptor write failed with status") == true)
    }


    @Test
    fun `onCharacteristicChanged emits notification`() = runBlocking {
        // Prepare a list to collect emitted notifications.
        val notifications = mutableListOf<Pair<UUID, ByteArray>>()
        // Launch a coroutine to collect from the shared flow.
        val deferred = async {
            callbackHandler.notificationFlow.first()
        }
        // Set up the mock characteristic to return testUuid and expectedNotificationData.
        whenever(mockCharacteristic.uuid).thenReturn(testUuid)
        whenever(mockCharacteristic.value).thenReturn(expectedNotificationData)
        // Allow time for the shared flow to emit.
        delay(1000)
        // Simulate a characteristic changed event.
        callbackHandler.onCharacteristicChanged(mockGatt, mockCharacteristic)

        notifications.add(deferred.await())

        // Verify that a notification was emitted.
        assertTrue("Notification should be emitted", notifications.isNotEmpty())
        val emitted = notifications.first()
        assertTrue("Emitted notification UUID should match", emitted.first == testUuid)
        assertArrayEquals("Emitted notification data should match expected data", expectedNotificationData, emitted.second)
    }


    @Test
    fun `awaitCharacteristicRead returns expected data on success`() = runBlocking {
        // Prepare an expected byte array value.
        val expectedData = byteArrayOf(0x01, 0x02, 0x03)
        // Create a random UUID for this characteristic.
        val testUuid = UUID.randomUUID()

        // Setup mockCharacteristic to return the expected UUID and value.
        whenever(mockCharacteristic.uuid).thenReturn(testUuid)
        whenever(mockCharacteristic.value).thenReturn(expectedData)

        // Launch a coroutine that awaits the read operation.
        val deferred = async { callbackHandler.awaitCharacteristicRead() }

        // Allow the coroutine to start and assign the deferred value.
        delay(500)

        // Simulate a successful read callback.
        callbackHandler.onCharacteristicRead(
            mockGatt,
            mockCharacteristic,
            BluetoothGatt.GATT_SUCCESS
        )

        // Await the result and assert it matches expected data.
        val result = deferred.await()
        assertArrayEquals("Characteristic value should match expected data", expectedData, result)
    }

    @Test(expected = Exception::class)
    fun `awaitCharacteristicRead throws exception on failure`() = runBlocking {
        // Create a random UUID for this characteristic.
        val testUuid = UUID.randomUUID()

        // Setup the mockCharacteristic with the test UUID.
        whenever(mockCharacteristic.uuid).thenReturn(testUuid)
        // Launch the awaitCharacteristicRead() call.
        val deferred = async { callbackHandler.awaitCharacteristicRead() }

        // Allow the coroutine to start and assign the deferred value.
        kotlinx.coroutines.delay(500)

        // Simulate a failure by invoking the callback with a failure status.
        callbackHandler.onCharacteristicRead(
            mockGatt,
            mockCharacteristic,
            BluetoothGatt.GATT_FAILURE
        )

        // Awaiting the deferred should throw an exception.
        deferred.await()
        // If no exception is thrown, the test should fail.
        fail("Expected an exception when characteristic read fails")
    }


}