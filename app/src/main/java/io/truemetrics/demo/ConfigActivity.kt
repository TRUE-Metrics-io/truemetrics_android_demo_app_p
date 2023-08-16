package io.truemetrics.demo

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import io.truemetrics.truemetricssdk.TruemetricsSDK

class ConfigActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_config)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        val configurationTextView = findViewById<TextView>(R.id.configuration)

        configurationTextView.text = TruemetricsSDK.getActiveConfig()?.rawResponse
    }
}