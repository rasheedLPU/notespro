package com.example.notespro

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.notespro.R
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener

class SignupActivity : AppCompatActivity() {

    private lateinit var regName : TextInputLayout
    private lateinit var regUsername : TextInputLayout
    private lateinit var regPassword : TextInputLayout
    private lateinit var regEmail : TextInputLayout
    private lateinit var regPhoneNo : TextInputLayout


    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        val signinBtn = findViewById<Button>(R.id.sign_in_btn)


        regName = findViewById(R.id.name)
        regUsername = findViewById(R.id.username)
        regEmail = findViewById(R.id.email)
        regPassword = findViewById(R.id.password)
        regPhoneNo = findViewById(R.id.phone_no)


        signinBtn.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

    }

    private fun validateName():Boolean{
        val name = regName.editText?.text.toString()

        if(name.isEmpty()){
            regName.error = "field cannot be empty"
            return false
        }

        regName.isErrorEnabled = false
        regName.error = null
        return true
    }


    private fun validateUsername():Boolean{
        val username = regUsername.editText?.text.toString()

        if(username.isEmpty()){
            regUsername.error = "field cannot be empty"
            return false
        }
        else if(username.length>=16){
            regUsername.error = "max length 15 "
            return false
        }
        else if(username.contains(" ")){
            regUsername.error = "cannot contain whitespace"
            return false
        }
        else{
            val charArray = username.toCharArray()

            for(i in charArray.indices)
                if(!charArray[i].isLetterOrDigit()){
                    regUsername.error = "cannot contain special character"
                    return false
                }
        }

        regUsername.isErrorEnabled = false
        regUsername.error = null
        return true
    }

    private fun validatePhoneNo():Boolean{
        val phone = regPhoneNo.editText?.text.toString()

        if(phone.isEmpty()){
            regPhoneNo.error = "field cannot be empty"
            return false
        }
        else if(phone.length != 10){
            regPhoneNo.error = "enter valid phone number"
            return false
        }

        regPhoneNo.isErrorEnabled = false
        regPhoneNo.error = null
        return true
    }

    private fun validateEmail():Boolean{
        val email = regEmail.editText?.text.toString()

        if(email.isEmpty()){
            regEmail.error = "field cannot be empty"
            return false
        }
        else if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            regEmail.error = "not a valid  email"
            return false
        }

        regEmail.isErrorEnabled = false
        regEmail.error = null
        return true
    }

    private fun validatePassword():Boolean{
        val password = regPassword.editText?.text.toString()

        if(password.isEmpty()){
            regPassword.error = "field cannot be empty"
            return false
        }
        else if(password.length<=7){
            regPassword.error = "min length 8"
            return false
        }
        else if(password.contains(" ")){
            regPassword.error = "cannot contain whitespace"
            return false
        }
        else{
            val charArray = password.toCharArray()
            var specialCharCount = 0
            var numberCount = 0
            var upperCaseCount = 0

            for(i in charArray.indices){

                if(!charArray[i].isLetterOrDigit())
                    specialCharCount++

                else if(charArray[i].isDigit())
                    numberCount++

                else if(charArray[i].isUpperCase())
                    upperCaseCount++
            }

            if(specialCharCount < 1){
                regPassword.error = "require at least one special character"
                return false
            }

            if(numberCount < 1){
                regPassword.error = "require at least one digit"
                return false
            }

            if(upperCaseCount< 1){
                regPassword.error = "require at least one uppercase"
                return false
            }

        }

        regPassword.isErrorEnabled = false
        regPassword.error = null
        return true
    }

    fun validateUser(view: View) {
        if(!validateName() || !validateUsername() || !validatePhoneNo() || !validateEmail() || !validatePassword()){
            return
        }

        val name = regName.editText!!.text.toString()
        val username = regUsername.editText!!.text.toString()
        val phoneNo = regPhoneNo.editText!!.text.toString()
        val email = regEmail.editText!!.text.toString()
        val password = regPassword.editText!!.text.toString()


        //check if user already exist
        val reference = FirebaseDatabase.getInstance().getReference("users")
        val checkUser : Query = reference.orderByChild("username").equalTo(username)

        checkUser.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    regUsername.error = "username already exist"
                    regUsername.requestFocus()
                }
                else{
                    val intent = Intent(applicationContext, VerifyPhoneNumberActivity::class.java)
                    intent.putExtra("name", name)
                    intent.putExtra("username", username)
                    intent.putExtra("phoneNo", phoneNo)
                    intent.putExtra("email", email)
                    intent.putExtra("password", password)
                    startActivity(intent)
                }
            }
            override fun onCancelled(error: DatabaseError) {}

        })
    }


}