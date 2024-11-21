package com.example.notespro

import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.notespro.EditNote
import com.example.notespro.NoteDataClass
import com.google.firebase.database.FirebaseDatabase
import java.util.UUID


class MyAdapter(private val list: ArrayList<NoteDataClass>, private val context: Context) : RecyclerView.Adapter<MyAdapter.MyViewHolder>() {

    private val pref = context.getSharedPreferences("login", AppCompatActivity.MODE_PRIVATE)
    val username = pref.getString("username" , null).toString()

    class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val noteTitle: TextView = view.findViewById(R.id.note_title)
        val noteText: TextView = view.findViewById(R.id.note_text)

        val cardView: CardView = view.findViewById(R.id.cardView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyAdapter.MyViewHolder {
        return MyViewHolder(LayoutInflater.from(context).inflate(R.layout.rv_item, parent,false))
    }

    override fun onBindViewHolder(holder: MyAdapter.MyViewHolder, position: Int) {
        holder.noteTitle.text = list[position].title
        holder.noteText.text = list[position].text

        holder.cardView.setOnClickListener {
            val intent = Intent(context, EditNote::class.java)
            intent.putExtra("title", list[position].title)
            intent.putExtra("text", list[position].text)
            intent.putExtra("key", list[position].key)

            context.startActivity(intent)
        }

        holder.cardView.setOnLongClickListener {

            deleteNote(position)

            return@setOnLongClickListener true
        }

    }

    override fun getItemCount(): Int {
        return list.size
    }

    private fun deleteNote(position: Int) {
        val options = arrayOf("Share Note", "Delete Note","Bluetooth transfer")

        val builder = AlertDialog.Builder(context)
            .setTitle("Choose Action")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> { // Share Note
                        shareNoteViaBluetooth(list[position])

                    }
                    1 -> { // Delete Note
                        val confirmDeleteDialog = AlertDialog.Builder(context)
                            .setTitle("Delete Note")
                            .setMessage("Do you want to delete the note?")
                            .setIcon(R.drawable.baseline_delete_24)
                            .setPositiveButton("Yes") { _, _ ->
                                FirebaseDatabase.getInstance().getReference("users")
                                    .child(username).child("noteList").child(list[position].key)
                                    .removeValue()

                                list.removeAt(position)
                                notifyItemRemoved(position)
                            }
                            .setNegativeButton("No") { _, _ -> }
                            .create()
                        confirmDeleteDialog.show()
                    }
                    2 ->{

                    }
                }
            }
        builder.create().show()
    }
    private fun shareNoteViaBluetooth(note: NoteDataClass) {
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_TEXT, "Title: ${note.title}\nContent: ${note.text}")

        val chooser = Intent.createChooser(intent, "Share Note via")
        if (context.packageManager.queryIntentActivities(chooser, 0).isNotEmpty()) {
            context.startActivity(chooser)
        } else {
            AlertDialog.Builder(context)
                .setTitle("No Sharing Apps Found")
                .setMessage("Please enable Bluetooth or install a compatible app.")
                .setPositiveButton("OK", null)
                .show()
        }
    }

}