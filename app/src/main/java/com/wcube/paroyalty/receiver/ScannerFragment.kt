package com.wcube.paroyalty.receiver

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment

class ScannerFragment : Fragment() {
    private var TAG = "beacon-receiver"
    private var UUID = "F7826DA6-4FA2-4E98-8024-BC5B71E0893E"
    private var btManager: BluetoothManager? = null
    private var btAdapter: BluetoothAdapter? = null
    private var btScanner: BluetoothLeScanner? = null
    private lateinit var uuidDisplay: TextView
    private lateinit var majorDisplay: TextView
    private lateinit var minorDisplay: TextView

    // necessary permissions on Android <12
    private val BLE_PERMISSIONS = arrayOf(
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    // necessary permissions on Android >=12
    private val ANDROID_12_BLE_PERMISSIONS = arrayOf(
        Manifest.permission.BLUETOOTH_SCAN,
        Manifest.permission.BLUETOOTH_CONNECT,
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.scanner_fragment, container, false)
        // check for permission to scan BLE
        if (container != null) {
            if (isPermissionGranted(container.context)) {
                setUpBluetoothManager()
            }
        }

        uuidDisplay =  view.findViewById(R.id.uuid) as TextView
        majorDisplay =  view.findViewById(R.id.major) as TextView
        minorDisplay =  view.findViewById(R.id.minor) as TextView

        return view
    }

    private fun isPermissionGranted(context: Context): Boolean {
        Log.d(TAG, "@isPermissionGranted: checking bluetooth")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if ((ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED) ||
                (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.BLUETOOTH_SCAN
                ) != PackageManager.PERMISSION_GRANTED)
            ) {
                Log.d(TAG, "@isPermissionGranted: requesting Bluetooth on Android >= 12")
                requestPermissions(ANDROID_12_BLE_PERMISSIONS, 2)
                return false
            }
        } else {
            if ((ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) || (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
                        )) {
                Log.d(TAG, "@isPermissionGranted: requesting Location on Android < 12")
                requestPermissions(BLE_PERMISSIONS, 3)
                return false
            }
        }
        Log.d(TAG, "@isPermissionGranted Bluetooth permission is ON")
        return true
    }

    private val leScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            val scanRecord = result?.scanRecord
            super.onScanResult(callbackType, result)

            if (scanRecord != null) {
                val device = IBeacon(scanRecord?.bytes!!)
                val uuid = device.getUUID()
                val major = device.getMajor()
                val minor = device.getMinor()

                if (uuid == UUID) {
                    uuidDisplay.setText("UUID: "+ uuid)
                    majorDisplay.setText("Major: "+ major)
                    minorDisplay.setText("Minor: "+ minor)

                    Log.d(TAG, "@receive Broadcast " + "Device UUID: " + uuid + "\n" + "Major: " + major + "\n" + "Minor: " + minor + "\n")
                }
            }
            return
        }

        override fun onScanFailed(errorCode: Int) {
            Log.d(TAG, "@onScanFailed failed: " + errorCode)
        }
    }

    private fun startBLEScan() {
        try {
            btScanner!!.startScan(leScanCallback)
        } catch (e: SecurityException) {
            Log.e(TAG, "@startScan SecurityException: " + e.message)
        }
    }

    private fun setUpBluetoothManager() {
        btManager = activity?.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        btAdapter = btManager!!.adapter
        btScanner = btAdapter?.bluetoothLeScanner
        if (btAdapter != null && btAdapter!!.isEnabled) {
            Log.d(TAG, "Bluetooth adapter is enabled")
            startBLEScan()
        }
    }

}