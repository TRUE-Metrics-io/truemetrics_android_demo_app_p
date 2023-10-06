package io.truemetrics.demo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.forEach
import io.truemetrics.demo.databinding.ActivityLogMetadataBinding
import io.truemetrics.truemetricssdk.TruemetricsSDK

class LogMetadataActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivityLogMetadataBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener { finish() }

        binding.addMoreFields.setOnClickListener {
            val fields = LayoutInflater.from(this).inflate(R.layout.view_metadata_fields, binding.fieldsContainer, false)

            fields.findViewById<View>(R.id.remove).setOnClickListener {
                binding.fieldsContainer.removeView(fields)
            }

            binding.fieldsContainer.addView(fields, binding.fieldsContainer.childCount - 1)
        }

        binding.logMetadata.setOnClickListener {
            val map = mutableMapOf<String, String>()
            binding.fieldsContainer.forEach { v ->

                if(v is LinearLayout) {
                    val keyEditText = v.findViewById<EditText>(R.id.key)
                    val valueEditText = v.findViewById<EditText>(R.id.value)

                    var key = ""
                    var value = ""

                    if(keyEditText != null){
                        key = keyEditText.text.toString().trim()
                    }

                    if(valueEditText != null){
                        value = valueEditText.text.toString().trim()
                    }

                    if (key.isNotEmpty() && value.isNotEmpty()) {
                        map[key] = value
                    }
                }
            }

            TruemetricsSDK.logMetadata(map)

            finish()
        }
    }
}