package com.kjipo.bluetoothmidi.ui.midirecord

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.ParcelUuid
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.selection.SelectionPredicates
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StorageStrategy
import androidx.recyclerview.widget.RecyclerView
import com.example.bluetoothmiditest.deviceList.*
import com.kjipo.bluetoothmidi.BluetoothDeviceData
import com.kjipo.bluetoothmidi.R
import com.kjipo.bluetoothmidi.devicelist.DeviceDataSource
import com.kjipo.bluetoothmidi.devicelist.DeviceListAdapter
import com.kjipo.bluetoothmidi.devicelist.DeviceListViewModel
import com.kjipo.bluetoothmidi.devicelist.DeviceListViewModelFactory
import timber.log.Timber
import java.util.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [MidiRecord.newInstance] factory method to
 * create an instance of this fragment.
 */
class MidiRecord : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private val scanFilter =
        ScanFilter.Builder().setServiceUuid(ParcelUuid(MIDI_OVER_BTLE_UUID)).build()


    private val bluetoothScanCallback = BluetoothScanCallback()

    private var isScanning = false

    private val deviceListViewModel by viewModels<DeviceListViewModel> {
        DeviceListViewModelFactory()
    }

    private val BluetoothAdapter.isDisabled: Boolean
        get() = !isEnabled

    private lateinit var bluetoothManager: BluetoothManager

    private lateinit var scanButton: Button

    private lateinit var tracker: SelectionTracker<String>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }

        bluetoothManager = context?.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager

        val bluetoothAdapter = bluetoothManager.adapter
        if (!bluetoothAdapter.isDisabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                // TODO Is it necessary to do anything here?

                Timber.i("Activity result: $it")

            }.launch(enableBtIntent)
        }

        val connectButton = requireView().findViewById<Button>(R.id.btnConnect)?.also {
            it.isEnabled = false
        }

        scanButton = requireView().findViewById<Button>(R.id.btnScan)!!.also {
            it.setOnClickListener {
                toggleScanningAndUpdateButtonText()
            }
        }

        val deviceView = requireView().findViewById<RecyclerView>(R.id.deviceView)
        val deviceListAdapter = DeviceListAdapter()
        deviceView.adapter = deviceListAdapter

        deviceListViewModel.deviceLiveData.observe(this) { liveData ->
            deviceListAdapter.submitList(liveData)
        }

        tracker = SelectionTracker.Builder(
            "selection",
            deviceView,
            DeviceListAdapter.KeyProvider(deviceListAdapter),
            DeviceDetailLookup(deviceView),
            StorageStrategy.createStringStorage()
        ).withSelectionPredicate(SelectionPredicates.createSelectSingleAnything()).build()
            .also { selectionTracker ->
                savedInstanceState?.let { savedState ->
                    selectionTracker.onRestoreInstanceState(savedState)
                }
                deviceListAdapter.tracker = selectionTracker

                selectionTracker.addObserver(object : SelectionTracker.SelectionObserver<String>() {

                    override fun onSelectionChanged() {
                        super.onSelectionChanged()
                        connectButton?.isEnabled = !tracker.selection.isEmpty
                    }
                })
            }

        connectButton?.setOnClickListener {
            tracker.selection.let { bluetoothDeviceSelection ->
                if (bluetoothDeviceSelection.isEmpty) {
                    Timber.w("Connect button pressed when device selection is empty")
                    return@let
                }
                DeviceDataSource.getDataSource()
                    .getDeviceList().value?.find { it.bluetoothDevice.address == bluetoothDeviceSelection.first() }
                    ?.let {
                        // Stop the scanning and open an activity that shows the MIDI data from
                        // the selected MIDI Bluetooth device
                        toggleScanningAndUpdateButtonText(false)

                        // TODO Replace acitivty with a fragment
//                        val openMidiDeviceIntent =
//                            Intent(requireContext().applicationContext, ShowDataActivity::class.java).apply {
//                                putExtra(Intent.EXTRA_TEXT, it.bluetoothDevice)
//                            }
//                        startActivity(openMidiDeviceIntent)
                    }
            }
        }


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val binding = inflater.inflate(R.layout.fragment_midi_record, container, false)

        return binding
    }

    private fun toggleScanningAndUpdateButtonText() {
        toggleScanningAndUpdateButtonText(!isScanning)
    }

    private fun toggleScanningAndUpdateButtonText(doScan: Boolean) {
        if (doScan == isScanning) {
            return
        }

        if (doScan) {
            Timber.i("Starting scanning")

            scanLeDevices(bluetoothManager.adapter)
            scanButton.setText(R.string.stop)
        } else {
            Timber.i("Stopping scanning")

            stopScanning()
            scanButton.setText(R.string.scan)
        }
    }


    private fun stopScanning() {
        context?.let {
            stopScanning(it)
        }
    }

    private fun stopScanning(context: Context) {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_SCAN
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        bluetoothManager.adapter.bluetoothLeScanner.stopScan(bluetoothScanCallback)
        isScanning = false
    }


    private fun scanLeDevices(bluetoothAdapter: BluetoothAdapter) {
        if (isScanning) {
            // Already scanning
            return
        }
        val leScanner = bluetoothAdapter.bluetoothLeScanner
        isScanning = true

        Timber.i("Start scan")

        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.BLUETOOTH_SCAN
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        leScanner.startScan(
            listOf(scanFilter),
            ScanSettings.Builder().build(),
            bluetoothScanCallback
        )
    }



    inner class BluetoothScanCallback : ScanCallback() {

        override fun onScanResult(
            callbackType: Int,
            result: ScanResult?
        ) {
            Timber.i("Scan result. Callback type: ${callbackType}. Result: $result")
            result?.apply {
                requireActivity().runOnUiThread {
                    BluetoothDeviceData(device).let {
                        DeviceDataSource.getDataSource().insertDevice(it)
                    }
                }
            }
        }

        override fun onBatchScanResults(results: List<ScanResult?>?) {
            Timber.i("Scan results. Results: $results")

            results?.apply {
                filterNotNull().forEach {
                    BluetoothDeviceData(it.device).let { bluetoothDeviceData ->
                        DeviceDataSource.getDataSource().insertDevice(bluetoothDeviceData)
                    }
                }
            }
        }

        override fun onScanFailed(errorCode: Int) {
            isScanning = false
            Timber.e("Scan failed. Error code: $errorCode")
        }
    }


    companion object {

        private const val SCAN_PERIOD = 10000L
        private val MIDI_OVER_BTLE_UUID = UUID.fromString("03B80E5A-EDE8-4B33-A751-6CE34EC4C700")


        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment MidiRecord.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            MidiRecord().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}