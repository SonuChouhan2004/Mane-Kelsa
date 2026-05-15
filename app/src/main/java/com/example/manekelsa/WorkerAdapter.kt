package com.example.manekelsa

import Worker
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import android.widget.ImageView
import android.net.Uri
import android.content.Intent
import androidx.core.content.ContextCompat
import android.graphics.Color
import android.widget.Toast
import com.google.firebase.database.FirebaseDatabase

class WorkerAdapter(private val workers: List<Worker>) :
    RecyclerView.Adapter<WorkerAdapter.WorkerViewHolder>() {

    class WorkerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgWorker: ImageView = itemView.findViewById(R.id.imgWorker)
        val txtName: TextView = itemView.findViewById(R.id.txtName)
        val txtWorkType: TextView = itemView.findViewById(R.id.txtWorkType)
        val txtRate: TextView = itemView.findViewById(R.id.txtRate)
        val txtLocation: TextView = itemView.findViewById(R.id.txtLocation)
        val availabilityDot: View = itemView.findViewById(R.id.availabilityDot)
        val btnLike: ImageView = itemView.findViewById(R.id.btnLike)
        val txtLikes: TextView = itemView.findViewById(R.id.txtLikes)
        val btnCall: Button = itemView.findViewById(R.id.btnCall)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WorkerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_worker_card, parent, false)
        return WorkerViewHolder(view)
    }

    override fun onBindViewHolder(holder: WorkerViewHolder, position: Int) {
        val worker = workers[position]

        holder.txtName.text = worker.name ?: "–"
        holder.txtWorkType.text = worker.workType ?: "–"
        holder.txtRate.text = "ದಿನದ ದರ: ರೂ. ${worker.rate ?: "–"}"
        holder.txtLocation.text = "ಪ್ರದೇಶ: ${worker.location ?: "–"}"
        holder.txtLikes.text = (worker.likes ?: 0).toString()
        val txtAvailable = holder.itemView.findViewById<TextView>(R.id.txtAvailable)
        val cardView = holder.itemView.findViewById<androidx.cardview.widget.CardView>(R.id.cardRoot)

        // Availability dot color
        if (worker.available) {
            holder.availabilityDot.background =
                ContextCompat.getDrawable(holder.itemView.context, R.drawable.bg_availability_dot)
            txtAvailable.text = "ಇಂದು ಲಭ್ಯವಿದೆ"
            txtAvailable.setTextColor(Color.parseColor("#008000")) // green text
            txtAvailable.background = ContextCompat.getDrawable(holder.itemView.context, R.drawable.bg_available)
            cardView.setCardBackgroundColor(Color.parseColor("#F8FFF8"))
            holder.btnCall.isEnabled = true
            holder.btnCall.backgroundTintList =
                ContextCompat.getColorStateList(holder.itemView.context, R.color.brown)
            holder.btnCall.setTextColor(Color.WHITE)
        } else {
            holder.availabilityDot.background = ContextCompat.getDrawable(holder.itemView.context, R.drawable.bg_unavailable_dot)
            txtAvailable.text = "ಇಂದು ಲಭ್ಯವಿಲ್ಲ"
            txtAvailable.setTextColor(Color.WHITE)
            txtAvailable.background = ContextCompat.getDrawable(holder.itemView.context, R.drawable.bg_unavailable_dot)
            cardView.setCardBackgroundColor(Color.parseColor("#5C5C3D"))
            holder.btnCall.isEnabled = false
            holder.btnCall.backgroundTintList =
                ContextCompat.getColorStateList(holder.itemView.context, R.color.grey_disabled)
            holder.btnCall.setTextColor(Color.parseColor("#E0E0E0"))
        }

        holder.btnLike.setOnClickListener {
            val context = holder.itemView.context
            val database = FirebaseDatabase.getInstance()
            val workerRef = database.getReference("workers").child(worker.phone ?: return@setOnClickListener)
            holder.btnLike.isEnabled = false
            // Increment likes in Firebase
            val newLikes = (worker.likes ?: 0) + 1
            workerRef.child("likes").setValue(newLikes)
                .addOnSuccessListener {
                    holder.txtLikes.text = newLikes.toString()
                    holder.btnLike.setColorFilter(ContextCompat.getColor(context, R.color.gold))
                    Toast.makeText(context, "Liked 👍", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Failed to update like", Toast.LENGTH_SHORT).show()
                }
        }


        // Call button
        holder.btnCall.setOnClickListener {
            worker.phone?.let { phone ->
                val intent = Intent(Intent.ACTION_DIAL)
                intent.data = Uri.parse("tel:$phone")
                holder.btnCall.context.startActivity(intent)
            }
        }
    }

    override fun getItemCount() = workers.size
}
