package com.example.gamelog

import java.io.Serializable

data class Oyun(
    var id: String = "",
    val kimeAit: String = "",
    val oyunAdi: String = "",
    val platform: String = "",
    val durum: String = "",
    val puan: String = "",
    val hikaye: String = "",
    val resimUrl: String = "" // <-- YENİ EKLENEN ÖZELLİK 🖼️
) : Serializable



