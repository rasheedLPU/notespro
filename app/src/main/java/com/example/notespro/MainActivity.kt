package com.example.notespro

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.example.notespro.R
import com.example.notespro.NoteFragment
import com.example.notespro.ProfileFragment
import com.example.notespro.MyAdapter
import com.google.android.material.bottomnavigation.BottomNavigationView

//add google fonts

class MainActivity : AppCompatActivity() {

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO) //turnoff night mode
        setContentView(R.layout.activity_main)

        val noteFragment = NoteFragment()
        val profileFragment = ProfileFragment()

        setFragment(noteFragment)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottonNav)

        bottomNav.setOnNavigationItemSelectedListener {
            when (it.itemId){
                R.id.note -> setFragment(noteFragment)
                R.id.profile -> setFragment(profileFragment)
            }
            true
        }
    }

    private  fun setFragment(fragment:Fragment){
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.frameLayout, fragment)
            commit()
        }
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 1) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permissions granted
            } else {
                Toast.makeText(this, "Bluetooth permissions are required to share notes", Toast.LENGTH_SHORT).show()
            }
        }
    }

}