package com.example.ble.utils

import android.Manifest
import android.bluetooth.BluetoothManager
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat


/**
 * This composable wraps the permission logic and checks if bluetooth it's available and enabled
 */
@Composable
fun BlePermissionBox(
    extraPermissions: List<String> = emptyList(),
    content: @Composable () -> Unit,
) {

    val context = LocalContext.current
    val packageManager = context.packageManager
    val bluetoothAdapter = context.getSystemService(BluetoothManager::class.java).adapter

    // Define the permissions needed for BLE.
    val requiredPermissions =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            mutableListOf<String>(
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_SCAN,
            ).also { it.addAll(extraPermissions) }
        }else {
            mutableListOf<String>(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
            ).also { it.addAll(extraPermissions) }
        }

    var allPermissionsGranted by remember { mutableStateOf(false) }

    // Check if all required permissions are granted.
    allPermissionsGranted = requiredPermissions.all { permission ->
        ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }


    // Create a launcher to request multiple permissions.
    val permissionsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        allPermissionsGranted = permissions.all { it.value }
    }

    if (allPermissionsGranted) {
        // All permissions granted.

        // Check to see if the Bluetooth classic feature is available.
        val hasBT = packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH)
        // Check to see if the BLE feature is available.
        val hasBLE = packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)
        // Check if the adapter is enabled
        var isBTEnabled by remember {
            // Screen should be changed when Bluetooth turns on,
            // save time to check every time the screen is shown.
            mutableStateOf(bluetoothAdapter?.isEnabled == true)
        }

        when {
            bluetoothAdapter == null || !hasBT ->
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ){
                    Text(modifier = Modifier.wrapContentSize(),
                        textAlign = TextAlign.Center,
                        text = "No bluetooth available")
                }
            !hasBLE ->
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ){
                    Text(modifier = Modifier.wrapContentSize(),
                        textAlign = TextAlign.Center,
                        text = "No bluetooth low energy available")
                }
            !isBTEnabled ->
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ){
                    Text(modifier = Modifier.wrapContentSize(),
                        textAlign = TextAlign.Center,
                        text = "Bluetooth is disabled")
                }
            else -> content()
        }
    } else {
        // If permissions are missing, show a simple UI to explain and request them.
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("This app requires location and Bluetooth permissions to scan and connect to BLE devices.")
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = {
                // Request the required permissions.
                permissionsLauncher.launch(requiredPermissions.toTypedArray())
            }) {
                Text("Grant Permissions")
            }
        }
    }


}
