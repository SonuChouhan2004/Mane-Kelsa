package com.example.manekelsa

import Worker
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.HorizontalScrollView
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*

class OwnerActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var workerList: ArrayList<Worker>
    private lateinit var allWorkers: ArrayList<Worker>
    private lateinit var adapter: WorkerAdapter

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_owner)

        // 🔹 Back button
        val backButton = findViewById<ImageView>(R.id.btnBack)
        backButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        // 🔹 RecyclerView setup
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        allWorkers = ArrayList()
        workerList = ArrayList()
        adapter = WorkerAdapter(workerList)
        recyclerView.adapter = adapter

        // 🔹 Chip buttons
        val btnAll = findViewById<Button>(R.id.btnAll)
        val btnBasavanagudi = findViewById<Button>(R.id.btnBasavanagudi)
        val btnJayanagar = findViewById<Button>(R.id.btnJayanagar)
        val btnRajajinagar = findViewById<Button>(R.id.btnRajajinagar)

        val chipButtons = listOf(btnAll, btnBasavanagudi, btnJayanagar, btnRajajinagar)

        chipButtons.forEach { button ->
            button.setOnClickListener {
                chipButtons.forEach {
                    it.isSelected = false
                    it.background = ContextCompat.getDrawable(this, R.drawable.bg_chip_unselected)
                    it.setTextColor(ContextCompat.getColor(this, R.color.brown))
                }
                button.isSelected = true
                button.background = ContextCompat.getDrawable(this, R.drawable.bg_chip_selected)
                button.setTextColor(ContextCompat.getColor(this, R.color.white))

                // 🔹 Filter workers
                val selectedText = button.text.toString()
                workerList.clear()
                if (selectedText == "ಎಲ್ಲಾ") {
                    workerList.addAll(allWorkers)
                } else {
                    workerList.addAll(allWorkers.filter { it.location == selectedText })
                }
                adapter.notifyDataSetChanged()
            }
        }

        // 🔹 Firebase data
        val workersRef = FirebaseDatabase.getInstance().getReference("workers")
        workersRef.addValueEventListener(object : ValueEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {
                allWorkers.clear()
                workerList.clear()
                Log.d("FirebaseData", "Snapshot exists: ${snapshot.exists()}")

                for (workerSnapshot in snapshot.children) {
                    Log.d("WorkerSnapshot", "Raw: ${workerSnapshot.value}")
                    val worker = workerSnapshot.getValue(Worker::class.java)
                    Log.d("WorkerParsed", "Parsed: $worker")
                    worker?.let { allWorkers.add(it) }
                }
                workerList.clear()
                workerList.addAll(allWorkers)
                Log.d("WorkerCount", "Loaded ${workerList.size} workers")
                adapter.notifyDataSetChanged()

                if (workerList.isEmpty()) {
                    Toast.makeText(this@OwnerActivity, "No workers found", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseError", "Error: ${error.message}")
            }
        })

        // 🔹 Scroll controls
        val scrollView = findViewById<HorizontalScrollView>(R.id.horizontalScrollView)
        val btnScrollLeft = findViewById<ImageButton>(R.id.btnScrollLeft)
        val btnScrollRight = findViewById<ImageButton>(R.id.btnScrollRight)
        val scrollIndicator = findViewById<SeekBar>(R.id.scrollIndicator)
        scrollIndicator.max = 100
        val scrollStep = resources.getDimensionPixelSize(R.dimen.chip_scroll_step)

        btnScrollLeft.setOnClickListener {
            scrollView.smoothScrollBy(-scrollStep, 0)
            updateScrollIndicator(scrollView, scrollIndicator)
        }
        btnScrollRight.setOnClickListener {
            scrollView.smoothScrollBy(scrollStep, 0)
            updateScrollIndicator(scrollView, scrollIndicator)
        }
        scrollView.viewTreeObserver.addOnScrollChangedListener {
            updateScrollIndicator(scrollView, scrollIndicator)
        }
    }

    fun updateScrollIndicator(scrollView: HorizontalScrollView, indicator: SeekBar) {
        val maxScroll = scrollView.getChildAt(0).width - scrollView.width
        if (maxScroll > 0) {
            val progress = (scrollView.scrollX.toFloat() / maxScroll * indicator.max).toInt()
            indicator.progress = progress
        }
    }
}
