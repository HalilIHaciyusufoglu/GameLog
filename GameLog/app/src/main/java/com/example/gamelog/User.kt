package com.example.gamelog

data class User(
    val email: String = "",
    val kullaniciAdi: String = "",
    val profilResmi: String = "",
    val isAdmin: Boolean = false
)