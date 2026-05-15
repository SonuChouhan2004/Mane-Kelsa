package com.example.manekelsa

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream

class EditProfileActivity : AppCompatActivity() {
    private lateinit var profileImage: ImageView
    private lateinit var cameraIcon: ImageView
    private val PICK_IMAGE = 100
    private val CAPTURE_IMAGE = 101

    private var selectedImageUri: Uri? = null
    private var capturedBitmap: Bitmap? = null

    private lateinit var workButtons: List<Button>
    private var selectedLocation: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        profileImage = findViewById(R.id.profileImage)
        cameraIcon = findViewById(R.id.cameraIcon)

        // Spinner setup
        val locationSpinner = findViewById<Spinner>(R.id.spinnerLocation)
        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.locations,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        locationSpinner.adapter = adapter

        // 🔹 Capture selected location properly
        locationSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                selectedLocation = parent.getItemAtPosition(position).toString()
            }
            override fun onNothingSelected(parent: AdapterView<*>) {
                selectedLocation = ""
            }
        }

        // Work type buttons
        workButtons = listOf(
            findViewById(R.id.btnCleaning),
            findViewById(R.id.btnGardening),
            findViewById(R.id.btnCooking),
            findViewById(R.id.btnStitching),
            findViewById(R.id.btnElectrician),
            findViewById(R.id.btnOther)
        )

        workButtons.forEach { button ->
            button.setOnClickListener {
                workButtons.forEach { it.isSelected = false }
                button.isSelected = true
            }
        }

        // Save button
        val saveButton = findViewById<Button>(R.id.btnSaveProfile)
        saveButton.setOnClickListener {
            val name = findViewById<EditText>(R.id.editName).text.toString()
            val phone = findViewById<EditText>(R.id.editPhone).text.toString()
            val rate = findViewById<EditText>(R.id.editRate).text.toString()
            val selectedWorkType = workButtons.firstOrNull { it.isSelected }?.text?.toString() ?: ""

            if (phone.isBlank()) {
                Toast.makeText(this, "Enter phone number", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val database = FirebaseDatabase.getInstance()
            val workerRef = database.getReference("workers").child(phone)

            fun saveWorker(profileUrl: String?) {
                val workerData = mutableMapOf<String, Any>(
                    "name" to name,
                    "phone" to phone,
                    "location" to selectedLocation,   // ✅ use selectedLocation
                    "rate" to rate,
                    "workType" to selectedWorkType,
                    "available" to false
                )
                profileUrl?.let { workerData["profileImageUrl"] = it }

                workerRef.setValue(workerData).addOnSuccessListener {
                    Log.d("FirebaseSave", "Saved worker: $workerData")
                    Toast.makeText(this, "Profile saved!", Toast.LENGTH_SHORT).show()

                    // ✅ Pass phone number to WorkerActivity
                    val intent = Intent(this, WorkerActivity::class.java)
                    intent.putExtra("phone", phone)
                    startActivity(intent)
                    finish()
                }.addOnFailureListener {
                    Log.e("FirebaseSave", "Error: ${it.message}")
                    Toast.makeText(this, "Error saving profile", Toast.LENGTH_SHORT).show()
                }
            }

            // Upload image if selected/captured
            val storageRef = FirebaseStorage.getInstance().reference.child("workers/$phone/profile.jpg")

            when {
                selectedImageUri != null -> {
                    storageRef.putFile(selectedImageUri!!)
                        .addOnSuccessListener {
                            storageRef.downloadUrl.addOnSuccessListener { uri ->
                                saveWorker(uri.toString())
                            }
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Image upload failed", Toast.LENGTH_SHORT).show()
                            saveWorker(null)
                        }
                }
                capturedBitmap != null -> {
                    val baos = ByteArrayOutputStream()
                    capturedBitmap!!.compress(Bitmap.CompressFormat.JPEG, 80, baos)
                    val data = baos.toByteArray()

                    storageRef.putBytes(data)
                        .addOnSuccessListener {
                            storageRef.downloadUrl.addOnSuccessListener { uri ->
                                saveWorker(uri.toString())
                            }
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Image upload failed", Toast.LENGTH_SHORT).show()
                            saveWorker(null)
                        }
                }
                else -> saveWorker(null)
            }
        }

        // Back button
        val backButton = findViewById<ImageView>(R.id.btnBack)
        backButton.setOnClickListener { finish() }

        // Gallery picker
        cameraIcon.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, PICK_IMAGE)
        }

        // Camera capture
        profileImage.setOnClickListener {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(intent, CAPTURE_IMAGE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                PICK_IMAGE -> {
                    selectedImageUri = data?.data
                    profileImage.setImageURI(selectedImageUri)
                }
                CAPTURE_IMAGE -> {
                    capturedBitmap = data?.extras?.get("data") as? Bitmap
                    profileImage.setImageBitmap(capturedBitmap)
                }
            }
        }
    }
}
