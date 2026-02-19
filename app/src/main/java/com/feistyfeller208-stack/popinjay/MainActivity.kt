package com.feistyfeller208.popinjay

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.security.MessageDigest
import java.util.UUID


class MainActivity : AppCompatActivity() {

    private lateinit var bluetoothManager: BluetoothManager
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var tvPopinjayId: TextView
    private lateinit var btnGenerateId: Button
    private lateinit var btnEnableBluetooth: Button

    private val REQUEST_ENABLE_BLUETOOTH = 1
    private val REQUEST_PERMISSION_CODE = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize views
        tvPopinjayId = findViewById(R.id.tvPopinjayId)
        btnGenerateId = findViewById(R.id.btnGenerateId)
        btnEnableBluetooth = findViewById(R.id.btnEnableBluetooth)

        // Initialize Bluetooth
        bluetoothManager = getSystemService(BluetoothManager::class.java)
        bluetoothAdapter = bluetoothManager.adapter

        // Check and request permissions
        checkPermissions()

        // Set up button click listeners
        btnGenerateId.setOnClickListener {
            generateAndShowPopinjayId()
        }

        btnEnableBluetooth.setOnClickListener {
            enableBluetooth()
        }

        // Load existing ID if any
        loadExistingId()
    }

    private fun checkPermissions() {
        val permissions = mutableListOf<String>()

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.BLUETOOTH_CONNECT)
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.BLUETOOTH_SCAN)
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADVERTISE) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.BLUETOOTH_ADVERTISE)
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        if (permissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissions.toTypedArray(), REQUEST_PERMISSION_CODE)
        }
    }

    private fun enableBluetooth() {
        if (!bluetoothAdapter.isEnabled) {
            val enableBtIntent = android.content.Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            ActivityCompat.startActivityForResult(this, enableBtIntent, REQUEST_ENABLE_BLUETOOTH, null)
        } else {
            Toast.makeText(this, "Bluetooth already enabled", Toast.LENGTH_SHORT).show()
        }
    }

    private fun generateAndShowPopinjayId() {
        val deviceName = bluetoothAdapter.name ?: "Unknown Device"
        val deviceAddress = bluetoothAdapter.address ?: UUID.randomUUID().toString()
        val uniqueString = "$deviceName-$deviceAddress-${System.currentTimeMillis()}"
        
        val popinjayId = hashString(uniqueString)
        
        // Save to SharedPreferences
        val prefs = getSharedPreferences("popinjay_prefs", MODE_PRIVATE)
        prefs.edit().putString("popinjay_id", popinjayId).apply()
        
        tvPopinjayId.text = "Your Popinjay ID:\n$popinjayId"
        
        Toast.makeText(this, "ID Generated Successfully", Toast.LENGTH_SHORT).show()
    }

    private fun loadExistingId() {
        val prefs = getSharedPreferences("popinjay_prefs", MODE_PRIVATE)
        val existingId = prefs.getString("popinjay_id", null)
        
        if (existingId != null) {
            tvPopinjayId.text = "Your Popinjay ID:\n$existingId"
        } else {
            tvPopinjayId.text = "No ID yet. Tap Generate."
        }
    }

    private fun hashString(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }.substring(0, 16).uppercase()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSION_CODE) {
            if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                Toast.makeText(this, "Permissions granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Permissions required for Bluetooth", Toast.LENGTH_LONG).show()
            }
        }
    }
}
