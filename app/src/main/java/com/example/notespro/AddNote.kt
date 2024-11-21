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

class AddNote : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_note)

        val title = findViewById<EditText>(R.id.add_note_title)
        val text = findViewById<EditText>(R.id.add_note_text)
        val btn = findViewById<MaterialButton>(R.id.add_btn)



        btn.setOnClickListener {
            val textStr = text.text.toString()
            val titleStr = title.text.toString()

            if(textStr.isNotEmpty() && titleStr.isNotEmpty()){

                val pref = getSharedPreferences("login", MODE_PRIVATE)
                val username = pref.getString("username", null)

                username?.let{
                    val encryptedTitle = EncryptAndDecrypt.encrypt(titleStr)
                    val encryptedText = EncryptAndDecrypt.encrypt(textStr)

//                    addNote(username, titleStr, textStr)
                    addNote(username, encryptedTitle, encryptedText)
                }

                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
            else{
                if (textStr.isEmpty()){
                    text.error = "cannot be empty"
                    text.requestFocus()
                }
                else if(titleStr.isEmpty()){
                    title.error = "cannot be empty"
                    title.requestFocus()
                }
            }
        }
    }

    private fun addNote(username:String, title:String, text:String) {
        FirebaseDatabase.getInstance().getReference("users")
            .child(username).child("noteList").push().setValue(NoteDataClass(title,text))
        //push generate new key/value every time when data is added
    }

}