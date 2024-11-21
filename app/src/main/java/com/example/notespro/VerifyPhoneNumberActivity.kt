package com.example.notespro

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.notespro.R
import com.example.notespro.UserDataClass
import com.example.notespro.EncryptAndDecrypt
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import com.google.firebase.auth.PhoneAuthProvider.ForceResendingToken
import com.google.firebase.auth.PhoneAuthProvider.OnVerificationStateChangedCallbacks
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import java.util.concurrent.TimeUnit


class VerifyPhoneNumberActivity : AppCompatActivity() {

    lateinit var progressBar:ProgressBar
    lateinit var verificationCodeBySystem:String
    private lateinit var mAuth:FirebaseAuth

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verify_phone_number)

        val verifyBtn = findViewById<Button>(R.id.verify_phone_btn)
        val enteredPhoneNo = findViewById<EditText>(R.id.otp)
        progressBar = findViewById(R.id.progress_bar)

        val phoneNo = intent.getStringExtra("phoneNo")

        mAuth = Firebase.auth

        verificationCodeBySystem = "123456"

        sendVerificationCodeToUser(phoneNo!!)

        verifyBtn.setOnClickListener {

            val code = enteredPhoneNo.text.toString()

            if (code.isEmpty() || code.length<6) {
                enteredPhoneNo.error = "wrong otp"
                enteredPhoneNo.requestFocus()
                return@setOnClickListener
            }

            progressBar.visibility = View.VISIBLE
            verifyCode(code)
        }


    }

    private fun sendVerificationCodeToUser(phoneNo: String) {
        Log.d("users" , "no +91$phoneNo")
        // [START start_phone_auth]
        val options = PhoneAuthOptions.newBuilder(mAuth)
            .setPhoneNumber("+91$phoneNo") // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(this) // Activity (for callback binding)
            .setCallbacks(mCallbacks) // OnVerificationStateChangedCallbacks
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
        // [END start_phone_auth]
    }

    private val mCallbacks: OnVerificationStateChangedCallbacks =
        object : OnVerificationStateChangedCallbacks() {
            override fun onCodeSent(s: String, forceResendingToken: ForceResendingToken) {
                super.onCodeSent(s, forceResendingToken)
                verificationCodeBySystem = s
            }

            override fun onVerificationCompleted(phoneAuthCredential: PhoneAuthCredential) {
                val code = phoneAuthCredential.smsCode
                if (code != null) {
                    progressBar.visibility = View.VISIBLE
                    verifyCode(code)
                }
            }

            override fun onVerificationFailed(e: FirebaseException) {
                Log.d("users12", e.message.toString())
            }
        }

    private fun verifyCode(codeByUser: String) {
        val credential = PhoneAuthProvider.getCredential(verificationCodeBySystem, codeByUser)
        signInTheUserByCredentials(credential)
    }

    private fun signInTheUserByCredentials(credential: PhoneAuthCredential) {
        val firebaseAuth = FirebaseAuth.getInstance()
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener {
            if (it.isSuccessful) {

                val phoneNo = intent.getStringExtra("phoneNo").toString()
                val name = intent.getStringExtra("name").toString()
                val username = intent.getStringExtra("username").toString()
                val password = intent.getStringExtra("password").toString()
                val email = intent.getStringExtra("email").toString()

                val encryptedName = EncryptAndDecrypt.encrypt(name);
                val encryptedPhoneNo = EncryptAndDecrypt.encrypt(phoneNo);
                val encryptedPassword = EncryptAndDecrypt.encrypt(password);
                val encryptedEmail = EncryptAndDecrypt.encrypt(email);

//                FirebaseDatabase.getInstance()
//                    .getReference("users").child(username)
//                    .setValue(UserDataClass(name,username, phoneNo,email,password, listOf()))

                FirebaseDatabase.getInstance()
                    .getReference("users").child(username)
                    .setValue(
                        UserDataClass(false,encryptedName, username, encryptedPhoneNo,
                            encryptedEmail, encryptedPassword, listOf())
                    )

                //after verifying upload to firebase


                Toast.makeText(applicationContext, "Your Account has been created successfully! login to continue", Toast.LENGTH_SHORT).show()
                //Perform Your required action here to either let the user sign In or do something required
                val intent = Intent(applicationContext, LoginActivity::class.java)

                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
            else {
                Log.d("user", it.exception?.message.toString())
                if(it.exception is FirebaseAuthInvalidCredentialsException){
                    Toast.makeText(applicationContext, "Wrong OTP!!", Toast.LENGTH_LONG).show()
                    progressBar.visibility = View.GONE
                }
            }
        }



    }
}