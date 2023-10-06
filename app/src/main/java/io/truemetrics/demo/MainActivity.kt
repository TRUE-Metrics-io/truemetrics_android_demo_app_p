package io.truemetrics.demo

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
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
        private const val BACKGROUND_LOCATION_REQUEST_CODE = 1
    }

    private lateinit var binding: ActivityMainBinding

    private val viewModel: MainViewModel by viewModels()
    private val prefs: SharedPreferences by lazy {
        this.getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE)
    }

    private var askBackgroundLocationPermission: Boolean = false

    private val permissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        Logger.d(TAG, "locationPermissionRequest $permissions")
        when {
            permissions.getOrDefault(android.Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                if(askBackgroundLocationPermission) {
                    requestBackgroundLocationPermission()
                }
            }

            permissions.getOrDefault(android.Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                if(askBackgroundLocationPermission) {
                    requestBackgroundLocationPermission()
                }
            }

            else -> {
                Logger.d(TAG, "locationPermissionRequest denied")
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

        @SuppressLint("InlinedApi")
        override fun askPermissions(permissions: List<String>) {
            Log.d(TAG, "askPermissions: $permissions")
            askBackgroundLocationPermission = permissions.contains(android.Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            val filteredPermissions = permissions.toMutableList()
            // ACCESS_BACKGROUND_LOCATION needs to be requested separately so we are removing it from the list
            filteredPermissions.remove(android.Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            permissionRequest.launch(filteredPermissions.toTypedArray())
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
                    3 -> startActivity(Intent(this, LogMetadataActivity::class.java))
                }
                true
            }
            menu.show()
        }

        viewModel.time.observe(this) {
            binding.currentTime.text = it
        }

        checkApiKeyAndInitSdk()
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

    private fun showApiKeyDialog(){
        val alertDialog = AlertDialog.Builder(this)
            .setCancelable(false)
            .create()

        val dialogView: View = LayoutInflater.from(this).inflate(R.layout.dialog_api_key, null)

        val editText = dialogView.findViewById<EditText>(R.id.api_key)

        dialogView.findViewById<View>(R.id.cancel_button)
            .setOnClickListener {
                alertDialog.cancel()
                finish()
            }

        dialogView.findViewById<View>(R.id.save_button).setOnClickListener {
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

    @SuppressLint("InlinedApi")
    private fun requestBackgroundLocationPermission(){
        AlertDialog.Builder(this)
            .setTitle(R.string.dialog_title_background_location_permission)
            .setMessage(R.string.dialog_message_background_location_permission)
            .setPositiveButton(R.string.dialog_settings) { _, _ ->
                requestPermissions(
                    arrayOf(android.Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                    BACKGROUND_LOCATION_REQUEST_CODE
                )
            }
            .setNegativeButton(R.string.dialog_cancel) { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }
}