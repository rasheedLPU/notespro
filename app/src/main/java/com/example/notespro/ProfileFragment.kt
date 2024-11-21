package com.example.notespro

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.notespro.R
import com.example.notespro.LoginActivity
import com.example.notespro.EncryptAndDecrypt
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.database.*


class ProfileFragment : Fragment() {

    lateinit var name:String
    lateinit var email:String
    lateinit var password:String
    lateinit var phoneNo:String
    lateinit var username:String

    lateinit var userName : TextInputLayout
    lateinit var userUsername: TextView
    lateinit var userEmail : TextInputLayout
    lateinit var userPassword : TextInputLayout
    lateinit var userPhoneNo : TextInputLayout

    private lateinit var reference : DatabaseReference

    private lateinit var updateBtn: Button

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        userName = view.findViewById(R.id.name)
        userUsername = view.findViewById(R.id.username)
        userEmail = view.findViewById(R.id.email)
        userPassword = view.findViewById(R.id.password)
        userPhoneNo = view.findViewById(R.id.phone_no)

        showUserData()

        val logOutBtn = view.findViewById<ImageView>(R.id.logout)
        logOutBtn.setOnClickListener {
            logOut()
        }

        updateBtn = view.findViewById(R.id.updateBtn)
        updateBtn.setOnClickListener{
            updateInfo()
        }

        return view
    }

    private fun showUserData(){

        val pref = this.activity?.getSharedPreferences("login", AppCompatActivity.MODE_PRIVATE)

        username  = pref?.getString("username", null).toString()

        reference = FirebaseDatabase.getInstance().getReference("users")

        val getData : Query = reference.orderByChild("username").equalTo(username)

        getData.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                username.let {
                    name =  snapshot.child(username).child("name").value.toString()
                    email = snapshot.child(username).child("email").value.toString()
                    phoneNo = snapshot.child(username).child("phoneNo").value.toString()
                    password = snapshot.child(username).child("password").value.toString()

                    val decryptedName = EncryptAndDecrypt.decrypt(name)
                    val decryptedEmail = EncryptAndDecrypt.decrypt(email)
                    val decryptedPhoneNo = EncryptAndDecrypt.decrypt(phoneNo)
                    val decryptedPassword = EncryptAndDecrypt.decrypt(password)

                    userUsername.text = username
                    userName.editText!!.setText(decryptedName)
                    userEmail.editText!!.setText(decryptedEmail)
                    userPassword.editText!!.setText(decryptedPassword)
                    userPhoneNo.editText!!.setText(decryptedPhoneNo)

                }
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e("sanskar", error.message)
            }

        })


    }

    private fun updateInfo(){

        val newName = userName.editText?.text.toString()
        val newPassword = userPassword.editText?.text.toString()
        val newEmail = userEmail.editText?.text.toString()

        if(newName.isEmpty() || newPassword.isEmpty() || newEmail.isEmpty()){
            Toast.makeText(activity, "value cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }

        Log.d("ProfileFragment", "newName: $newName")
        Log.d("ProfileFragment", "newPassword: $newPassword")
        Log.d("ProfileFragment", "newEmail: $newEmail")

        Log.e("sanskar", newName)

        val encryptedName = EncryptAndDecrypt.encrypt(newName)
        val encryptedEmail = EncryptAndDecrypt.encrypt(newEmail)
        val encryptedPassword = EncryptAndDecrypt.encrypt(newPassword)

        FirebaseDatabase.getInstance().getReference("users").child(username)
            .child("name").setValue(encryptedName)


        FirebaseDatabase.getInstance().getReference("users").child(username)
            .child("email").setValue(encryptedEmail)

        FirebaseDatabase.getInstance().getReference("users").child(username)
            .child("password").setValue(encryptedPassword)

        Toast.makeText(activity, "updated successfully !!!!", Toast.LENGTH_SHORT).show()

        showUserData()

        userName.clearFocus()
        userEmail.clearFocus()
        userPassword.clearFocus()
    }

    private fun logOut() {

        val builder = activity?.let {
            AlertDialog.Builder(it)
                .setTitle("sign out??")
                .setMessage("Do you want to exit?")
                .setIcon(R.drawable.baseline_logout_24)
                .setPositiveButton("yes") { _, _ ->

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
                .setNegativeButton("no") { _, _ -> }
        }

        val alertDialog = builder?.create()
        alertDialog?.show()

    }
}