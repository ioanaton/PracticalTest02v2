package ro.pub.cs.systems.eim.practicaltest02v2

import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import okhttp3.*
import org.json.JSONArray
import java.io.IOException

class PracticalTest02v2MainActivity : AppCompatActivity() {

    private lateinit var definitionReceiver: DefinitionReceiver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_practical_test02v2_main)

        // Inițializarea receiver-ului
        definitionReceiver = DefinitionReceiver()
        definitionReceiver.definitionOutput = findViewById(R.id.definitionOutput)

        // Înregistrarea receiver-ului pentru a primi broadcast-uri
        ContextCompat.registerReceiver(
            this,
            definitionReceiver,
            IntentFilter("com.example.ACTION_SEND_DEFINITION"),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )

        // Când se apasă butonul de căutare
        findViewById<Button>(R.id.searchButton).setOnClickListener {
            val word = findViewById<EditText>(R.id.wordInput).text.toString()
            if (word.isNotBlank()) {
                fetchDefinition(word)
            }
        }
    }



    private fun fetchDefinition(word: String) {
        val url = "https://api.dictionaryapi.dev/api/v2/entries/en/$word"
        val client = OkHttpClient()
        val request = Request.Builder().url(url).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("FetchDefinition", "Request failed", e)
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    response.body?.string()?.let { jsonResponse ->
                        try {
                            // Parsăm răspunsul JSON
                            val jsonArray = JSONArray(jsonResponse)
                            if (jsonArray.length() > 0) {
                                val definition = jsonArray.getJSONObject(0)
                                    .getJSONArray("meanings")
                                    .getJSONObject(0)
                                    .getJSONArray("definitions")
                                    .getJSONObject(0)
                                    .getString("definition")
                                Log.d("FetchDefinition", "Definition: $definition")
                                sendDefinitionBroadcast(definition)
                            } else {
                                Log.e("FetchDefinition", "No data available for the word")
                                sendDefinitionBroadcast("No definition found.")
                            }
                        } catch (e: Exception) {
                            Log.e("FetchDefinition", "Error parsing JSON response", e)
                            sendDefinitionBroadcast("Error fetching definition.")
                        }
                    }
                } else {
                    Log.e("FetchDefinition", "Response not successful")
                    sendDefinitionBroadcast("Error fetching definition.")
                }
            }
        })
    }

    private fun sendDefinitionBroadcast(definition: String) {
        val intent = Intent("com.example.ACTION_SEND_DEFINITION").apply {
            putExtra("definition", definition)
        }
        sendBroadcast(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(definitionReceiver) // Dezregistrăm receiver-ul la distrugerea activității
    }
}
