package ro.pub.cs.systems.eim.practicaltest02v2

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.TextView

class DefinitionReceiver : BroadcastReceiver() {

    var definitionOutput: TextView? = null

    override fun onReceive(context: Context?, intent: Intent?) {
        val definition = intent?.getStringExtra("definition")
        definitionOutput?.text = definition ?: "No definition found."
    }
}
