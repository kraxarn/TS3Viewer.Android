package com.crow.ts3_viewer

import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.crow.ts3_viewer.info.InfoAdapter
import com.crow.ts3_viewer.servers.ServersAdapter
import com.crow.ts3_viewer.servers.ServersEntry
import com.crow.ts3_viewer.ts3.Ts3
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import org.slf4j.LoggerFactory
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity()
{
    /**
     * Text inputs used in the "add" dialog
     */
    private lateinit var textInputs: Array<TextInputLayout>

    private var addDialog: AlertDialog? = null

    private lateinit var addLoad: LinearLayout

    private var ts3: Ts3? = null

    private val serverList = mutableListOf<ServersEntry>()

    private val serverListAdapter = ServersAdapter(serverList)

    private lateinit var textNoServers: TextView

    // Progress stuffs

    private lateinit var serverInfoLoad: ProgressBar

    private lateinit var serverInfo: BottomSheetBehavior<LinearLayout>

    private lateinit var serverInfoList: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.title = "${getString(R.string.app_name)} - Welcome"

        val addDialogView = layoutInflater.inflate(R.layout.dialog_add, findViewById(R.id.parent), false)

        textInputs = arrayOf(
            addDialogView.findViewById(R.id.text_layout_add_ip),
            addDialogView.findViewById(R.id.text_layout_add_query)
        )
        addLoad = addDialogView.findViewById(R.id.layout_add_load)

        // Setup server info sheet
        val serverInfoView = findViewById<LinearLayout>(R.id.layout_server_info)
        serverInfo = BottomSheetBehavior.from(serverInfoView)
        serverInfo.state = BottomSheetBehavior.STATE_HIDDEN
        serverInfoLoad = findViewById(R.id.progress_info_load)

        // Check if no servers added yet
        textNoServers = findViewById(R.id.text_no_servers)
        if (serverList.isEmpty())
            textNoServers.visibility = View.VISIBLE

        findViewById<RecyclerView>(R.id.list_servers).apply {
            adapter       = serverListAdapter
            layoutManager = LinearLayoutManager(this@MainActivity)
        }

        serverInfoList = findViewById(R.id.view_server_info)
        LinearLayoutManager(this).let {
            serverInfoList.layoutManager = it
            serverInfoList.addItemDecoration(DividerItemDecoration(this, it.orientation))
        }

        // Scary stuff
        findViewById<FloatingActionButton>(R.id.fab_add).setOnClickListener {
            // If we already dismissed it
            if (addDialog != null)
                (addDialogView.parent as ViewGroup).removeAllViews()
            // Just to make sure it's all good
            setTextInputsEnabled(true)
            // Create and show dialog
            addDialog = MaterialAlertDialogBuilder(this).apply {
                setTitle("Add New Server")
                setTheme(R.style.AppTheme_Dialog)
                setView(addDialogView)
                setPositiveButton("Add", null)
                setNegativeButton("Cancel", null)
            }.create().apply {
                setOnShowListener {
                    val dialog = addDialog as AlertDialog
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {view ->
                        // Pressed "add"
                        val errorText = dialog.findViewById<TextView>(R.id.text_add_error)
                        errorText?.visibility = View.GONE
                        setTextInputsEnabled(false)
                        view.isEnabled = false
                        // Create TS3 API instance
                        ts3 = Ts3(
                            dialog.findViewById<TextInputEditText>(R.id.edit_text_add_ip)
                                ?.text.toString()
                        )
                        // Try to connect (on another thread)
                        thread(true)
                        {
                            if (ts3!!.connect())
                            {
                                serverList.add(ts3!!.toEntry(this@MainActivity))
                                this@MainActivity.runOnUiThread {
                                    textNoServers.visibility = View.GONE
                                    serverListAdapter.notifyDataSetChanged()
                                    dialog.dismiss()
                                }
                                // For now, this is all we get
                                ts3?.exit()
                                ts3 = null
                            }
                            else
                            {
                                // Failed to connect
                                view.post {
                                    errorText?.visibility = View.VISIBLE
                                    setTextInputsEnabled(true)
                                    view.isEnabled = true
                                }
                            }
                        }
                    }
                }
                show()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean
    {
        menuInflater.inflate(R.menu.menu_app_options, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean
    {
        return when (item.itemId)
        {
            R.id.menu_app_about -> {
                AlertDialog.Builder(this)
                    .setTitle(getString(R.string.app_name))
                    .setView(layoutInflater.inflate(R.layout.dialog_about, findViewById(android.R.id.content), false))
                    .setNeutralButton("View Source") { _, _ ->
                        startActivity(Intent(Intent.ACTION_VIEW)
                            .setData(Uri.parse("https://github.com/kraxarn/Ts3Viewer.Android")))
                    }
                    .setPositiveButton("OK", null)
                    .show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setTextInputsEnabled(enabled: Boolean)
    {
        for (input in textInputs)
            input.isEnabled = enabled

        addLoad.visibility = if (enabled) View.GONE else View.VISIBLE
    }

    fun showServerInfo(ts3: Ts3)
    {
        // Start showing loading
        serverInfo.state = BottomSheetBehavior.STATE_EXPANDED

        // Actually load
        thread(true) {
            ts3.connect()
            val channels = ts3.channels
            ts3.exit()
            serverInfoList.post {
                serverInfoLoad.visibility = View.GONE
                serverInfoList.adapter = InfoAdapter(channels)
            }
        }
    }
}