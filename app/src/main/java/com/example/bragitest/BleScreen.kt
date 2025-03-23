package com.example.bragitest

import android.bluetooth.BluetoothAdapter.STATE_CONNECTED
import android.bluetooth.BluetoothAdapter.STATE_DISCONNECTED
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.ble.domain.model.BleDevice
import com.example.ble.presentation.BleViewModel
import java.util.UUID

@Composable
fun BleScreen(viewModel: BleViewModel = hiltViewModel()) {
    // Collect reactive state from the ViewModel
    val scanResults by viewModel.scanResults.collectAsState(initial = emptyList())
    val connectedDevice by viewModel.connectedDevice.collectAsState()

    // Local states for characteristic operations.
    var readValue by remember { mutableStateOf("") }
    var writeInput by remember { mutableStateOf("") }
    var writeResult by remember { mutableStateOf("") }
    var notificationResult by remember { mutableStateOf("") }
    var enabledResult by remember { mutableStateOf("") }

    val serviceUuidString = "0-0-0-0-0"           // Example: Battery Service
    val characteristicUuidString = "0-0-0-0-0"    // Example: Battery Level
    val serviceUUID = remember { UUID.fromString(serviceUuidString) }
    val characteristicUUID = remember { UUID.fromString(characteristicUuidString) }

    // Collect notifications and update local state.
    LaunchedEffect(viewModel.notifications) {
        viewModel.notifications.collect { (uuid, data) ->
            notificationResult = "UUID: $uuid, Data: ${data.joinToString(", ")}"
        }
    }

    Scaffold(modifier = Modifier.fillMaxSize().padding(20.dp)) { innerPadding ->

        Column(modifier = Modifier.fillMaxSize().padding(innerPadding),
            verticalArrangement = Arrangement.spacedBy(10.dp)) {

            // Display current connection status
            Text(
                text = "Connected Device: " +
                        (if (connectedDevice == BleDevice.None)
                            "None" else connectedDevice.name ?: connectedDevice.address)
            )


            // Row of buttons to start and stop scanning
            Row(modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { viewModel.startScan() }) {
                    Text("Start Scan")
                }
                Button(onClick = { viewModel.stopScan() }) {
                    Text("Stop Scan")
                }
            }

            // If a device is connected, show the characteristic operations.
            if (connectedDevice.connectionState == STATE_CONNECTED) {
                Button(onClick = { viewModel.disConnectToDevice() }) {
                    Text("Disconnect")
                }

                Text(
                    text = "Characteristic Operations",
                    style = MaterialTheme.typography.headlineSmall
                )

                // Read Characteristic Section
                Text(
                    text = "Read Characteristic",
                    style = MaterialTheme.typography.titleMedium
                )
                Button(onClick = {
                    viewModel.readCharacteristic(serviceUUID, characteristicUUID) { data ->
                        readValue = data?.joinToString(", ") ?: "Read Failed"
                    }
                }) {
                    Text("Read")
                }
                Text(text = "Read Value: $readValue", style = MaterialTheme.typography.bodyLarge)

                // Write Characteristic Section
                Text(
                    text = "Write Characteristic",
                    style = MaterialTheme.typography.titleMedium
                )
                OutlinedTextField(
                    value = writeInput,
                    onValueChange = { writeInput = it },
                    label = { Text("Enter value to write") },
                    modifier = Modifier.fillMaxWidth()
                )
                Button(onClick = {
                    val data = writeInput.toByteArray(Charsets.UTF_8)
                    viewModel.writeCharacteristic(serviceUUID, characteristicUUID, data) { success ->
                        writeResult = if (success) "Write Successful" else "Write Failed"
                    }
                }) {
                    Text("Write")
                }
                Text(text = "Write Result: $writeResult", style = MaterialTheme.typography.bodyLarge)

                // Notification Section
                Text(
                    text = "Set Notifications",
                    style = MaterialTheme.typography.titleMedium
                )

                Button(onClick = {
                    viewModel.setNotification(serviceUUID, characteristicUUID, true) { success ->
                        enabledResult = if (success) "Enable Successful" else "Enable Failed"
                    }
                }) {
                    Text("Enable Notifications")
                }
                Text(text = "Enable Result: $enabledResult", style = MaterialTheme.typography.bodyLarge)

                Text(
                    text = "Latest Notification: $notificationResult",
                    style = MaterialTheme.typography.bodyMedium
                )
            }else {
                // Display a list of scanned BLE devices.
                LazyColumn(modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(scanResults) { device ->
                        Row(modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.connectToDevice(device) {
                                // Handle connection result if needed.
                            } }
                            .padding(8.dp)) {
                            Text(text = device.name ?: device.address)
                        }
                    }
                }

            }




        }

    }

}