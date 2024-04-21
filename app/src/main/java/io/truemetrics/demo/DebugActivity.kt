package io.truemetrics.demo

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.AdapterDataObserver
import androidx.recyclerview.widget.RecyclerView.OnScrollListener
import androidx.recyclerview.widget.RecyclerView.SCROLL_STATE_IDLE
import io.truemetrics.demo.databinding.ActivityDebugBinding
import io.truemetrics.demo.sensors.SensorsActivity
import io.truemetrics.truemetricssdk.logger.FileLogger
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class DebugActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "DebugActivity"
    }

    private val viewModel: DebugViewModel by viewModels()

    private var progressDialog: ProgressDialog? = null

    private val exportDatabase = registerForActivityResult(ActivityResultContracts.CreateDocument("application/zip")) {
        Log.d(TAG, "exportDatabase target Uri: $it")
        if(it != null) {
            viewModel.saveDbToExternalStorage(this, it)
        } else {
            Toast.makeText(this, getString(R.string.couldnt_create_file), Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivityDebugBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val adapter = LogAdapter()

        var canScrollVertically = false

        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.export_logs -> exportLogs()
                R.id.export_db -> exportDatabase.launch("Truemetrics_recordings_db.zip")
                R.id.sensors -> startActivity(Intent(this, SensorsActivity::class.java))
            }
            true
        }

        binding.scrollToBottom.isVisible = false
        binding.scrollToBottom.setOnClickListener {
            binding.recyclerView.scrollToPosition(adapter.itemCount - 1)
            canScrollVertically = false
            binding.scrollToBottom.isVisible = false
        }

        binding.recyclerView.adapter = adapter
        adapter.registerAdapterDataObserver(object : AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                Log.d("DebugActivity", "onItemRangeInserted = $canScrollVertically")

                if (!canScrollVertically) {
                    binding.recyclerView.scrollToPosition(adapter.itemCount - 1)
                }
            }
        })

        binding.recyclerView.addOnScrollListener(object : OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (newState == SCROLL_STATE_IDLE) {
                    canScrollVertically = recyclerView.canScrollVertically(1)
                    Log.d("DebugActivity", "canScrollVertically = $canScrollVertically")
                    binding.scrollToBottom.isVisible = canScrollVertically
                }
            }
        })

        viewModel.recordingsCount.observe(this) {
            binding.recordingsInDb.text = it.toString()
        }

        viewModel.logMessages.observe(this) {
            adapter.setItems(it)
        }

        viewModel.dbSize.observe(this) {
            binding.dbSize.text = it
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                viewModel.freeStorage.collectLatest {
                    binding.freeStorage.text = it
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {

                viewModel.dbExportResult.collectLatest {
                    Log.d(TAG, "fileSaveResult: $it")
                    when(it) {
                        FileOpStatus.Started -> {
                            progressDialog = ProgressDialog(this@DebugActivity).also { pd ->
                                pd.setCancelable(false)
                                pd.setMessage(getString(R.string.exporting_databse))
                                pd.show()
                            }
                        }
                        FileOpStatus.Finished -> {
                            Toast.makeText(this@DebugActivity, getString(R.string.database_exported), Toast.LENGTH_SHORT).show()
                            progressDialog?.cancel()
                        }
                        is FileOpStatus.Error -> {
                            Toast.makeText(this@DebugActivity, it.message, Toast.LENGTH_SHORT).show()
                            progressDialog?.cancel()
                        }
                    }
                }
            }
        }
    }

    private fun exportLogs(){
        Intent(Intent.ACTION_SEND).apply {
            type = "*/email"
            putExtra(Intent.EXTRA_EMAIL, arrayOf("android-sdk-logs@truemetrics.io"))
            putExtra(Intent.EXTRA_SUBJECT, "Truemetrics SDK ${BuildConfig.VERSION_NAME} logs")
            putExtra(Intent.EXTRA_STREAM, FileLogger.getShareableLogsZipFile())
            if (resolveActivity(packageManager) != null) {
                startActivity(this)
            }
        }
    }
}