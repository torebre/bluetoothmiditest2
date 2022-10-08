package com.kjipo.bluetoothmidi

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat

class MainActivity : AppCompatActivity() {

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        requestPermissions()

        val appContainer = (application as BluetoothMidiApplication).container
        setContent {
            val widthSizeClass = calculateWindowSizeClass(this).widthSizeClass
            BluetoothMidiApp(appContainer, widthSizeClass)
        }


    }


    private fun requestPermissions() {
        val requiredPermissions = listOf(
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,

            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
//            Manifest.permission.BLUETOOTH_CONNECT,
//            Manifest.permission.BLUETOOTH_SCAN
        )

        val permissionsToRequest = requiredPermissions.filter { permission ->
            ContextCompat.checkSelfPermission(
                this,
                permission
            ) != PackageManager.PERMISSION_GRANTED
        }.toTypedArray()

        ActivityCompat.requestPermissions(this, permissionsToRequest, 2)
    }
}