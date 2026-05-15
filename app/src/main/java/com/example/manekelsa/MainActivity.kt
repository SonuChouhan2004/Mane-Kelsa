package com.example.manekelsa

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val ownerCard = findViewById<LinearLayout>(R.id.ownerCard)
        val btnWorker = findViewById<LinearLayout>(R.id.btnWorker)

        ownerCard.setOnClickListener {
            val intent = Intent(this, OwnerActivity::class.java)
            startActivity(intent)
        }

        btnWorker.setOnClickListener {
            val intent = Intent(this, WorkerActivity::class.java)
            startActivity(intent)
        }
    }
}
