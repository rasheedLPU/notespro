package com.example.notespro

import android.content.Context.SENSOR_SERVICE
import android.annotation.SuppressLint
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class NoteFragment : Fragment(), SensorEventListener {

    private lateinit var username: String
    private lateinit var sensorManager: SensorManager
    private lateinit var square: TextView
    private val decryptedNoteList = mutableListOf<NoteDataClass>()

    private lateinit var noteRV: RecyclerView
    private val noteKeySet = mutableSetOf<String>()
    private lateinit var progressBar: ProgressBar

    private lateinit var rootView: View // For updating background color dynamically

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rootView = inflater.inflate(R.layout.fragment_note, container, false)

        // UI Initialization
        progressBar = rootView.findViewById(R.id.progress_bar)
        progressBar.visibility = View.VISIBLE

        val pref = this.activity?.getSharedPreferences("login", MODE_PRIVATE)
        username = pref?.getString("username", null).toString()

        val addBtn = rootView.findViewById<MaterialButton>(R.id.add_btn)
        noteRV = rootView.findViewById(R.id.note_rv)
        square = rootView.findViewById(R.id.tv_square)

        noteRV.layoutManager = StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL)
        noteRV.adapter = MyAdapter(decryptedNoteList as ArrayList, activity as MainActivity)

        // Load notes from Firebase
        loadNote()

        addBtn?.setOnClickListener {
            val intent = Intent(activity, AddNote::class.java)
            startActivity(intent)
        }

        // Floating Action Button for Color Selector
        val fab = rootView.findViewById<FloatingActionButton>(R.id.fab)
        fab.setOnClickListener {
            showColorSelectorDialog()
        }

        // Accelerometer Initialization
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        sensorManager = requireActivity().getSystemService(SENSOR_SERVICE) as SensorManager
        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)?.also {
            sensorManager.registerListener(
                this,
                it,
                SensorManager.SENSOR_DELAY_NORMAL,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        }

        return rootView
    }

    private fun showColorSelectorDialog() {
        val colors = arrayOf("White", "Red", "Blue", "Green", "Yellow")
        val colorValues = arrayOf(Color.WHITE, Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW)

        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Select Background Color")
        builder.setItems(colors) { dialog, which ->
            // Update background color and dismiss dialog
            rootView.setBackgroundColor(colorValues[which])
            dialog.dismiss() // Close the dialog after a selection
        }
        builder.create().show()
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            val sides = event.values?.getOrNull(0) ?: 0f
            val upDown = event.values?.getOrNull(1) ?: 0f

            if (upDown.toInt() == 0 && sides.toInt() == 0) {
                FirebaseDatabase.getInstance().getReference("users").child(username)
                    .child("loggedIn").setValue(false)

                val intent = Intent(activity, LoginActivity::class.java)

                val pref =
                    this.activity?.getSharedPreferences("login", AppCompatActivity.MODE_PRIVATE)
                val editor = pref?.edit()
                editor?.putString("username", null)
                editor?.apply()

                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit

    override fun onResume() {
        super.onResume()
        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)?.also {
            sensorManager.registerListener(
                this,
                it,
                SensorManager.SENSOR_DELAY_NORMAL,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onDestroy() {
        sensorManager.unregisterListener(this)
        super.onDestroy()
    }

    private fun loadNote() {
        val firebaseRef = FirebaseDatabase.getInstance().getReference("users").child(username).child("noteList")
        firebaseRef.addListenerForSingleValueEvent(object : ValueEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (childSnapshot in dataSnapshot.children) {
                    val noteTitle = childSnapshot.child("title").getValue(String::class.java)
                    val noteText = childSnapshot.child("text").getValue(String::class.java)
                    val key = childSnapshot.key

                    if (noteTitle != null && noteText != null && key != null && !noteKeySet.contains(key)) {
                        val decryptedTitle = EncryptAndDecrypt.decrypt(noteTitle)
                        val decryptedText = EncryptAndDecrypt.decrypt(noteText)
                        decryptedNoteList.add(NoteDataClass(decryptedTitle, decryptedText, key))
                        noteKeySet.add(key)
                    }
                }
                noteRV.adapter?.notifyDataSetChanged()
                progressBar.visibility = View.GONE
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.d("user", databaseError.toString())
                progressBar.visibility = View.GONE
            }
        })
    }
}
