package com.example.notespro

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.notespro.R
import com.example.notespro.MainActivity
import com.example.notespro.EncryptAndDecrypt
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.database.*

class LoginActivity : AppCompatActivity() {

    lateinit var loginUsername: TextInputLayout
    lateinit var loginPassword: TextInputLayout

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        loginUsername = findViewById(R.id.username)
        loginPassword = findViewById(R.id.password)

        val loginBtn = findViewById<Button>(R.id.login_btn)


        val singupBtn = findViewById<Button>(R.id.sign_up_btn)
        singupBtn.setOnClickListener {
            val intent = Intent(this, SignupActivity::class.java)
            startActivity(intent)
        }

        loginBtn.setOnClickListener{
            if(!validateUsername() || !validatePassword())
                return@setOnClickListener
            isUser()
        }

    }

    private fun validateUsername():Boolean{
        val username = loginUsername.editText?.text.toString()

        if(username.isEmpty()){
            loginUsername.error = "field cannot be empty"
            return false
        }

        loginUsername.isErrorEnabled = false
        loginUsername.error = null
        return true
    }

    private fun validatePassword():Boolean{
        val password = loginPassword.editText?.text.toString()

        if(password.isEmpty()){
            loginPassword.error = "field cannot be empty"
            return false
        }

        loginPassword.isErrorEnabled = false
        loginPassword.error = null
        return true
    }

    private fun isUser() {

        var username = loginUsername.editText?.text.toString().trim()
        var password = loginPassword.editText?.text.toString().trim()


        password = EncryptAndDecrypt.encrypt(password)

        val reference = FirebaseDatabase.getInstance().getReference("users")


        val checkUser : Query = reference.orderByChild("username").equalTo(username) // get all record that equal to username

        checkUser.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    val passwordFromDB = snapshot.child(username).child("password").value.toString()
                    val loggedIn = snapshot.child(username).child("loggedIn").value.toString()

                    if(password == passwordFromDB){

                        if(loggedIn == "false"){

                            FirebaseDatabase.getInstance().getReference("users").child(username)
                                .child("loggedIn").setValue(true)

                            val intent = Intent(applicationContext, MainActivity::class.java)
                            intent.putExtra("username" , username)

                            val pref = getSharedPreferences("login" , MODE_PRIVATE)
                            val editor = pref.edit()
                            editor.putString("username" , snapshot.child(username).child("username").value.toString())
                            editor.apply()
                            //username is use to fetch data of user and maintain sign in and signup


                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                            finish()
                        }
                        else{
                            loginUsername.error = "user already logged in..."
                            loginUsername.requestFocus()
                            return
                        }
                    }
                    else{
                        loginPassword.error = "wrong password"
                        loginPassword.requestFocus()
                    }

                }
                else{
                    loginUsername.error = "user not exist"
                    loginPassword.requestFocus()
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })

    }

}