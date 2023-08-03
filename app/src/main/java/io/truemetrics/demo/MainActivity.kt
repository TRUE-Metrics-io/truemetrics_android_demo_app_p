package io.truemetrics.demo

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import io.truemetrics.truemetricssdk.ErrorCode
import io.truemetrics.truemetricssdk.StatusListener
import io.truemetrics.truemetricssdk.TruemetricsSDK
import io.truemetrics.truemetricssdk.config.Config
import io.truemetrics.truemetricssdk.engine.state.State
import java.util.Date

class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MainActivity"
    }

    private val actionButton by lazy { findViewById<ImageView>(R.id.action_button) }
    private val statusLabel by lazy { findViewById<TextView>(R.id.status) }
    private val timeLabel by lazy { findViewById<TextView>(R.id.time_label) }
    private val time by lazy { findViewById<TextView>(R.id.time) }

    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>

    private val statusListener = object : StatusListener{
        override fun onStateChange(state: State) {
            statusLabel.text = "SDK Status: $state"

            when(state) {
                State.RECORDING_IN_PROGRESS -> {
                    actionButton.setImageResource(R.drawable.ic_button_stop)
                    timeLabel.setText(R.string.label_start_time)
                    time.text = TruemetricsSDK.getRecordingStartTime().toString()
                }
                else -> {
                    actionButton.setImageResource(R.drawable.ic_button_start)
                    timeLabel.setText(R.string.label_current_time)
                    time.text = Date().toString()
                }
            }
        }

        override fun onError(errorCode: ErrorCode, message: String?) {
            showErrorDialog(errorCode, message)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (it) {
                startRecording()
            } else {
                Toast.makeText(
                    this,
                    R.string.toast_notifications_permission_needed,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        actionButton.setOnClickListener {
            if(TruemetricsSDK.isRecordingInProgress()) {
                TruemetricsSDK.stopRecording()
            } else {
                startRecording()
            }
        }

        if(!TruemetricsSDK.isInitialized()) {
            val notification = NotificationsHelper(this).createForegroundServiceNotification()

            TruemetricsSDK.initialize(
                this, Config(
                    apiKey = "YOUR_API_KEY",
                    foregroundNotification = notification
                )
            )
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
        NotificationsHelper(this).createNotificationChannels()
        TruemetricsSDK.startRecording()
    }

    private fun showErrorDialog(errorCode: ErrorCode, message: String?) {
        AlertDialog.Builder(this)
            .setTitle(R.string.error_dialog_title)
            .setMessage("error code: ${errorCode.name}, message: $message")
            .setNeutralButton(R.string.error_dialog_ok, null)
            .show()
    }
}