package com.void200.runku.hr

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.Build
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.util.UUID

/**
 * BLE heart-rate monitor via the standard GATT Heart Rate Service (0x180D).
 *
 * Works with Polar H10, Garmin HRM, Coospo, and most BLE straps/watches.
 * Fully offline — no network required.
 */
class BleHeartRate(context: Context) {

    private val manager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val adapter: BluetoothAdapter? = manager.adapter

    companion object {
        val HR_SERVICE: UUID = UUID.fromString("0000180d-0000-1000-8000-00805f9b34fb")
        val HR_MEASUREMENT: UUID = UUID.fromString("00002a37-0000-1000-8000-00805f9b34fb")
        val CCCD: UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")
    }

    private var gatt: BluetoothGatt? = null
    private var scanCallback: ScanCallback? = null

    /**
     * Emits BPM values. Scans for the first device advertising the HR service,
     * connects, subscribes to measurements.
     */
    @SuppressLint("MissingPermission")
    fun stream(): Flow<Int> = callbackFlow {
        if (adapter == null || !adapter.isEnabled) {
            close(); return@callbackFlow
        }
        val scanner = adapter.bluetoothLeScanner ?: run { close(); return@callbackFlow }

        val cb = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                if (result.scanRecord?.serviceUuids?.any { it.uuid == HR_SERVICE } == true) {
                    scanner.stopScan(this)
                    connectAndSubscribe(result.device)
                }
            }
        }
        scanCallback = cb
        scanner.startScan(null, ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build(), cb)

        awaitClose {
            runCatching { scanCallback?.let { scanner.stopScan(it) } }
            runCatching { gatt?.disconnect(); gatt?.close() }
            gatt = null
        }
    }

    @SuppressLint("MissingPermission")
    private fun connectAndSubscribe(device: android.bluetooth.BluetoothDevice) {
        gatt = device.connectGatt(null, false, object : BluetoothGattCallback() {
            override fun onConnectionStateChange(g: BluetoothGatt, status: Int, newState: Int) {
                if (newState == BluetoothProfile.STATE_CONNECTED) g.discoverServices()
            }

            override fun onServicesDiscovered(g: BluetoothGatt, status: Int) {
                val ch = g.getService(HR_SERVICE)?.getCharacteristic(HR_MEASUREMENT) ?: return
                g.setCharacteristicNotification(ch, true)
                val desc = ch.getDescriptor(CCCD) ?: return
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    g.writeDescriptor(desc, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
                } else {
                    @Suppress("DEPRECATION")
                    desc.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                    @Suppress("DEPRECATION")
                    g.writeDescriptor(desc)
                }
            }

            override fun onCharacteristicChanged(
                g: BluetoothGatt,
                characteristic: BluetoothGattCharacteristic,
                value: ByteArray,
            ) {
                parseHr(value)?.let { trySend(it) }
            }

            @Deprecated("pre-33")
            override fun onCharacteristicChanged(
                g: BluetoothGatt,
                characteristic: BluetoothGattCharacteristic,
            ) {
                @Suppress("DEPRECATION")
                parseHr(characteristic.value)?.let { trySend(it) }
            }
        })
    }

    /** Decode Heart Rate Measurement per Bluetooth SIG spec. */
    private fun parseHr(data: ByteArray?): Int? {
        if (data == null || data.isEmpty()) return null
        val flags = data[0].toInt()
        return if (flags and 0x01 == 0) {
            if (data.size >= 2) data[1].toInt() and 0xFF else null
        } else {
            if (data.size >= 3) ((data[1].toInt() and 0xFF) or ((data[2].toInt() and 0xFF) shl 8)) else null
        }
    }
}
