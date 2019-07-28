package com.crow.ts3_viewer

import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
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

/**
 * Toggles the view depending on if there are any items
 */
class ServerListObserver(private val view: View) : RecyclerView.AdapterDataObserver()
{
    override fun onItemRangeInserted(positionStart: Int, itemCount: Int)
    {
        update(itemCount)
        super.onItemRangeInserted(positionStart, itemCount)
    }

    fun update(itemCount: Int)
    {
        view.visibility = if (itemCount == 0) View.VISIBLE else View.GONE
    }
}

class MainActivity : AppCompatActivity()
{
    /**
     * Temporary re-usable TS3 instance
     */
    private var ts3: Ts3? = null

    /**
     * List of server for the recycler view
     */
    private val serverList = mutableListOf<ServersEntry>()

    /**
     * Server list adapter
     */
    private val serverListAdapter = ServersAdapter(serverList)

    //region Server info
    // This is here since we reuse the same stuff

    private lateinit var serverInfoLoad: ProgressBar

    private lateinit var serverInfo: BottomSheetBehavior<LinearLayout>

    private lateinit var serverInfoList: RecyclerView

    //endregion

    override fun onCreate(savedInstanceState: Bundle?)
    {
        // Create and inflate etc.
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Set the action bar title
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.title = "${getString(R.string.app_name)} - Welcome"

        // Setup server info sheet
        val serverInfoView = findViewById<LinearLayout>(R.id.layout_server_info)
        serverInfo = BottomSheetBehavior.from(serverInfoView)
        serverInfo.state = BottomSheetBehavior.STATE_HIDDEN
        serverInfoLoad = findViewById(R.id.progress_info_load)

        // TODO: Load from file here

        // Testing stuffs
        serverListAdapter.registerAdapterDataObserver(ServerListObserver(findViewById(R.id.text_no_servers)).apply {
            this.update(serverList.size)
        })

        // Setup server list
        findViewById<RecyclerView>(R.id.list_servers).apply {
            adapter       = serverListAdapter
            layoutManager = LinearLayoutManager(this@MainActivity)
        }

        // Setup the list for channels/users in server info
        serverInfoList = findViewById(R.id.view_server_info)
        LinearLayoutManager(this).let {
            serverInfoList.layoutManager = it
            serverInfoList.addItemDecoration(DividerItemDecoration(this, it.orientation))
        }

        // Scary stuff
        findViewById<FloatingActionButton>(R.id.fab_add).setOnClickListener {
            // Inflate view
            val addDialogView = layoutInflater.inflate(R.layout.dialog_add, findViewById(R.id.parent), false)
            val textInputs = arrayOf<TextInputLayout>(
                addDialogView.findViewById(R.id.text_layout_add_ip),
                addDialogView.findViewById(R.id.text_layout_add_query)
            )
            val addLoad = addDialogView.findViewById<LinearLayout>(R.id.layout_add_load)
            // Create and show dialog
            MaterialAlertDialogBuilder(this).apply {
                setTitle("Add New Server")
                setTheme(R.style.AppTheme_Dialog)
                setView(addDialogView)
                setPositiveButton("Add", null)
                setNegativeButton("Cancel", null)
            }.create().apply {
                setOnShowListener {
                    getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener { view ->
                        // Pressed "add"
                        // Save error text for now and hide it
                        val errorText = findViewById<TextView>(R.id.text_add_error)
                        errorText?.visibility = View.GONE
                        // Disable text entries while loading
                        setTextInputsEnabled(textInputs, addLoad, false)
                        view.isEnabled = false
                        // Create TS3 API instance
                        ts3 = Ts3(
                            findViewById<TextInputEditText>(R.id.edit_text_add_ip)?.text.toString(),
                            null,
                            findViewById<TextInputEditText>(R.id.edit_text_add_query)?.text
                                .toString().toInt()
                        )
                        // Try to connect (on another thread)
                        thread(true)
                        {
                            if (ts3!!.connect())
                            {
                                // If successful, add to server list
                                serverList.add(ts3!!.toEntry(this@MainActivity))
                                this@MainActivity.runOnUiThread {
                                    serverListAdapter.notifyItemInserted(serverList.size - 1)
                                    dismiss()
                                }
                                // For now, this is all we get
                                ts3?.exit()
                                ts3 = null
                                // Save new server to file first
                                Config(this@MainActivity).save(serverList.map {
                                    it.data
                                })
                            }
                            else
                            {
                                // Failed to connect, show error and enable text entries
                                view.post {
                                    errorText?.visibility = View.VISIBLE
                                    setTextInputsEnabled(textInputs, addLoad, true)
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

    /**
     * Loads the menu from the action bar
     */
    override fun onCreateOptionsMenu(menu: Menu?): Boolean
    {
        menuInflater.inflate(R.menu.menu_app_options, menu)
        return true
    }

    /**
     * Button in the action bar menu was pressed
     */
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

    private fun setTextInputsEnabled(textInputs: Array<TextInputLayout>, addLoad: View, enabled: Boolean)
    {
        for (input in textInputs)
            input.isEnabled = enabled

        addLoad.visibility = if (enabled) View.GONE else View.VISIBLE
    }

    /**
     * Opens the server info for the specified TS3 instance
     */
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