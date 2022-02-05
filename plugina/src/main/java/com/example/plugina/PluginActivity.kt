package com.example.plugina

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast

class PluginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_plugin)
        println(R.layout.activity_plugin)
        val button = findViewById<Button>(R.id.button)
        button.setOnClickListener {
            Toast.makeText(this, "hello plugin", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, Plugin1Activity::class.java))
        }

    }
}