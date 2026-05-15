package com.example.manekelsa

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.google.firebase.database.*

class WorkerActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_worker)

        val switchAvailability = findViewById<SwitchCompat>(R.id.switchAvailability)
        val txtAvailabilityStatus = findViewById<TextView>(R.id.txtAvailabilityStatus)
        val toggleHint = findViewById<TextView>(R.id.toggleHint)
        val availabilityCard = findViewById<LinearLayout>(R.id.availabilityCard)
        val workCount = findViewById<TextView>(R.id.txtWorkCount)
        val txtLocation = findViewById<TextView>(R.id.txtWorkType) // shows ಪ್ರದೇಶ

        // Default values
        workCount.text = "0"
        txtLocation.text = "–"

        val editProfileText = findViewById<TextView>(R.id.txtEditProfile)
        editProfileText.setOnClickListener {
            val intent = Intent(this, EditProfileActivity::class.java)
            startActivity(intent)
        }

        // ✅ Get dynamic phone number passed from EditProfileActivity
        val phone = intent.getStringExtra("phone") ?: "defaultPhone"
        val database = FirebaseDatabase.getInstance()
        val workerRef = database.getReference("workers").child(phone)

        // 🔹 Listen for realtime updates to location
        workerRef.child("location").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val location = snapshot.getValue(String::class.java)
                txtLocation.text = location ?: "–"
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@WorkerActivity, "Failed to load location", Toast.LENGTH_SHORT).show()
            }
        })

        // 🔹 Availability toggle
        switchAvailability.setOnCheckedChangeListener { _, isChecked ->
            workerRef.child("available").setValue(isChecked)

            if (isChecked) {
                txtAvailabilityStatus.text = getString(R.string.availability_on)
                txtAvailabilityStatus.setTextColor(ContextCompat.getColor(this, R.color.white))
                toggleHint.text = getString(R.string.availability_hint_on)
                toggleHint.setTextColor(ContextCompat.getColor(this, R.color.white))
                availabilityCard.setBackgroundResource(R.drawable.bg_green_card)
            } else {
                txtAvailabilityStatus.text = getString(R.string.availability_off)
                txtAvailabilityStatus.setTextColor(ContextCompat.getColor(this, R.color.dark_gray))
                toggleHint.text = getString(R.string.availability_hint)
                toggleHint.setTextColor(ContextCompat.getColor(this, R.color.dark_gray))
                availabilityCard.setBackgroundResource(R.drawable.bg_grey_card)
            }
        }

        // 🔹 Back button
        val backButton = findViewById<ImageView>(R.id.btnBack)
        backButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        // Default OFF state
        txtAvailabilityStatus.text = getString(R.string.availability_off)
        txtAvailabilityStatus.setTextColor(ContextCompat.getColor(this, R.color.dark_gray))
        toggleHint.text = getString(R.string.availability_hint)
        toggleHint.setTextColor(ContextCompat.getColor(this, R.color.dark_gray))
        availabilityCard.setBackgroundResource(R.drawable.bg_grey_card)
    }
}
