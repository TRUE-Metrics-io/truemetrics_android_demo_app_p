package io.truemetrics.demo

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import io.truemetrics.demo.databinding.ActivityMainBinding
import io.truemetrics.truemetricssdk.ErrorCode
import io.truemetrics.truemetricssdk.StatusListener
import io.truemetrics.truemetricssdk.TruemetricsSDK
import io.truemetrics.truemetricssdk.config.Config
import io.truemetrics.truemetricssdk.engine.state.State
import io.truemetrics.truemetricssdk.logger.Logger

class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MainActivity"
        private const val PREFS_FILE = "Prefs"
        private const val API_KEY = "apiKey"
    }

    private lateinit var binding: ActivityMainBinding

    private val viewModel: MainViewModel by viewModels()
    private val prefs: SharedPreferences by lazy {
        this.getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE)
    }

    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        Logger.d(TAG, "locationPermissionRequest $permissions")
        when {
            permissions.getOrDefault(android.Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                checkApiKeyAndInitSdk()
            }

            permissions.getOrDefault(android.Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                checkApiKeyAndInitSdk()
            }

            else -> {
                Logger.d(TAG, "locationPermissionRequest denied")
                checkApiKeyAndInitSdk()
            }
        }
    }

    private val statusListener = object : StatusListener{
        override fun onStateChange(state: State) {
            binding.status.text = "SDK Status: $state"

            when(state) {
                State.INITIALIZED -> {
                    TruemetricsSDK.startRecording()
                }
                State.RECORDING_IN_PROGRESS -> {
                    binding.startTimeLabel.setText(R.string.label_start_time)
                    binding.startTime.text = TruemetricsSDK.getRecordingStartTime().toString()
                }
                else -> {
                    binding.startTimeLabel.text = ""
                    binding.startTime.text = ""
                }
            }
        }

        override fun onError(errorCode: ErrorCode, message: String?) {
            showErrorDialog(errorCode, message)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)

        binding.moreMenu.setOnClickListener {
            val menu = PopupMenu(this, binding.moreMenu)

            if (TruemetricsSDK.isRecordingInProgress()) {
                menu.menu.add(0, 0, 0, "Stop recording")
            } else {
                menu.menu.add(0, 0, 0, "Start recording")
            }

            menu.menu.add(0, 1, 1, "Debug Log")
            menu.menu.add(0, 2, 2, "Active Configuration")
            menu.menu.add(0, 3, 3, "Log metadata")
            menu.setOnMenuItemClickListener {
                when(it.itemId) {
                    0 -> {
                        if(TruemetricsSDK.isRecordingInProgress()) {
                            TruemetricsSDK.stopRecording()
                        } else {
                            startRecording()
                        }
                    }
                    1 -> startActivity(Intent(this, DebugActivity::class.java))
                    2 -> startActivity(Intent(this, ConfigActivity::class.java))
                    3 -> showAddMetadataDialog()
                }
                true
            }
            menu.show()
        }

        viewModel.time.observe(this) {
            binding.currentTime.text = it
        }

        checkLocationPermissions()
    }

    private fun checkApiKeyAndInitSdk(){
        Logger.d(TAG, "checkApiKeyAndInitSdk")

        val apiKey = prefs.getString(API_KEY, "")
        if(apiKey!!.isNotEmpty()) {
            initSdk(apiKey)
        } else {
            showApiKeyDialog()
        }
    }

    override fun onResume() {
        super.onResume()
        TruemetricsSDK.setStatusListener(statusListener)
    }

    override fun onPause() {
        super.onPause()
        TruemetricsSDK.setStatusListener(null)
    }

    private fun startRecording(){
        TruemetricsSDK.startRecording()
    }

    private fun showErrorDialog(errorCode: ErrorCode, message: String?) {
        AlertDialog.Builder(this)
            .setTitle(R.string.error_dialog_title)
            .setMessage("error code: ${errorCode.name}, message: $message")
            .setNeutralButton(R.string.error_dialog_ok, null)
            .show()
    }

    private fun checkLocationPermissions() {
        Logger.d(TAG, "checkLocationPermissions")
        val hasFineLocationPermission = ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if(!hasFineLocationPermission) {
            locationPermissionRequest.launch(
                arrayOf(
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        } else {
            checkApiKeyAndInitSdk()
        }
    }

    private fun showApiKeyDialog(){
        val alertDialog = AlertDialog.Builder(this)
            .setCancelable(false)
            .create()

        val dialogView: View = LayoutInflater.from(this).inflate(R.layout.dialog_api_key, null)

        val editText = dialogView.findViewById<EditText>(R.id.api_key)

        dialogView.findViewById<View>(R.id.cancel_button)
            .setOnClickListener { _: View? ->
                alertDialog.cancel()
                finish()
            }

        dialogView.findViewById<View>(R.id.save_button).setOnClickListener { _: View? ->
            alertDialog.cancel()

            val apiKey = editText.text.toString()

            if(apiKey.isEmpty()){
                finish()
            } else {
                prefs.edit().putString(API_KEY, apiKey).apply()
                initSdk(editText.text.toString())
            }
        }

        alertDialog.setView(dialogView)
        alertDialog.show()
    }

    private fun showAddMetadataDialog(){
        val alertDialog = AlertDialog.Builder(this)
            .create()

        val dialogView: View = LayoutInflater.from(this).inflate(R.layout.dialog_add_metadata, null)

        val metadataKey = dialogView.findViewById<EditText>(R.id.metadata_key)
        val metadataValue = dialogView.findViewById<EditText>(R.id.metadata_value)

        dialogView.findViewById<View>(R.id.cancel_button)
            .setOnClickListener { _: View? ->
                alertDialog.cancel()
            }

        dialogView.findViewById<View>(R.id.add_button).setOnClickListener { _: View? ->

            val key = metadataKey.text.toString()
            val value = metadataValue.text.toString()

            if(key.isNotEmpty() && value.isNotEmpty()){
                TruemetricsSDK.logMetadata(mapOf(key to value))
                alertDialog.cancel()
            }
        }

        alertDialog.setView(dialogView)
        alertDialog.show()
    }

    private fun initSdk(apiKey: String){

        if(TruemetricsSDK.isInitialized()) {
            return
        }

        NotificationsHelper(this).createNotificationChannels()
        val notification = NotificationsHelper(this).createForegroundServiceNotification()

        val deviceId = TruemetricsSDK.initialize(
            this, Config(
                apiKey = apiKey,
                foregroundNotification = notification,
                debug = true
            )
        )

        binding.deviceId.text = deviceId
    }
}