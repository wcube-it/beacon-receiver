package com.wcube.paroyalty.receiver

import android.Manifest
import android.app.Activity.RESULT_OK
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import pub.devrel.easypermissions.EasyPermissions


class ScannerFragment : Fragment(), EasyPermissions.PermissionCallbacks {
    private var TAG = "beacon-receiver"
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

    val BLUETOOTH_PERMISSIONS_S =
        arrayOf(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.ACCESS_FINE_LOCATION)


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

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // EasyPermissions handles the request result.
        Log.d(TAG, "result"+ grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {
        // permission to scan granted, run bluetooth manager
        Log.d(TAG, "@isPermissionGranted: run scan")
        setUpBluetoothManager()
    }

    override fun onPermissionsDenied(requestCode: Int, perms: List<String>) {
        // TODO: if request rejected
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        // permission to turn on bluetooth succesd, run scan
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == 100) {
            startBLEScan()
        }
    }

    private fun isPermissionGranted(context: Context): Boolean {
        Log.d(TAG, "@isPermissionGranted: checking bluetooth")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!EasyPermissions.hasPermissions(context, *BLUETOOTH_PERMISSIONS_S)) {
                Log.d(TAG, "@isPermissionGranted: requesting Bluetooth on Android >= 12")
                EasyPermissions.requestPermissions(this, "message", 2, *BLUETOOTH_PERMISSIONS_S);
                return false
            }
        } else {
            if (!EasyPermissions.hasPermissions(context, *BLE_PERMISSIONS)) {
                Log.d(TAG, "@isPermissionGranted: requesting Bluetooth on Android >= 12")
                EasyPermissions.requestPermissions(this, "message", 2, *BLE_PERMISSIONS);
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
            Log.d(TAG, "receiving result")

            if (scanRecord != null) {
                val device = IBeacon(scanRecord?.bytes!!)
                val uuid = device.getUUID()
                val major = device.getMajor()
                val minor = device.getMinor()

                if (minor == 100) {
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
        if (btAdapter != null ) {
            if (btAdapter?.isEnabled == false) {
                Log.d(TAG, "Bluetooth adapter is not enabled")
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(enableBtIntent, 100)
            } else {
                Log.d(TAG, "Bluetooth adapter is enabled")
                startBLEScan()
            }
        }
    }
}