package com.example.notespro

data class UserDataClass(
    val loggedIn:Boolean,
    val name:String="",
    val username:String="",
    val phoneNo:String="",
    val email:String="",
    val password:String="",
    val noteList: List<NoteDataClass> = listOf()
)
