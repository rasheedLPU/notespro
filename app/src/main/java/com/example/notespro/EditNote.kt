package com.example.notespro

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.example.notespro.R
import com.example.notespro.NoteDataClass
import com.example.notespro.EncryptAndDecrypt
import com.google.android.material.button.MaterialButton
import com.google.firebase.database.FirebaseDatabase

class EditNote : AppCompatActivity() {



    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_note)

        val title = findViewById<EditText>(R.id.edit_note_title)
        val text = findViewById<EditText>(R.id.edit_note_text)
        val btn = findViewById<MaterialButton>(R.id.edit_btn)

        val titleIntent = intent.getStringExtra("title")
        val textIntent = intent.getStringExtra("text")
        val keyIntent = intent.getStringExtra("key").toString()

        title.setText(titleIntent)
        text.setText(textIntent)

        val pref = getSharedPreferences("login", MODE_PRIVATE)

        val username = pref.getString("username", null).toString()


        btn.setOnClickListener {
            val titleStr = title.text.toString()
            val textStr = text.text.toString()

            if(textStr.isNotEmpty() && titleStr.isNotEmpty()){

                val encryptedTitle = EncryptAndDecrypt.encrypt(titleStr)
                val encryptedText = EncryptAndDecrypt.encrypt(textStr)

//                updateNote(username,keyIntent,titleStr,textStr)

                updateNote(username, keyIntent, encryptedTitle, encryptedText)

                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
            else{

                if(titleStr.isEmpty()){
                    title.error = "Can not be empty"
                    title.requestFocus()
                }
                else if(textStr.isEmpty()){
                    text.error = "cannot be empty"
                }

            }
        }
    }

    private fun updateNote(username:String, key_intent:String, title: String, text:String){
        FirebaseDatabase.getInstance().getReference("users")
            .child(username).child("noteList").child(key_intent)
            .setValue(NoteDataClass(title, text, key_intent))
    }
}