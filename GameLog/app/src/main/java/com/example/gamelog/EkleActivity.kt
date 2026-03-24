package com.example.gamelog

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.gamelog.databinding.ActivityEkleBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class EkleActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEkleBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEkleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        val platformlar = listOf("PC", "PlayStation 5", "Xbox Series", "Nintendo Switch", "Mobil")
        binding.spPlatform.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, platformlar)

        val durumlar = listOf("Bitirdim", "Oynuyorum", "İstek Listesi", "Bıraktım")
        binding.spDurum.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, durumlar)

        binding.btnKaydet.setOnClickListener {
            val ad = binding.etOyunAdi.text.toString()
            val platform = binding.spPlatform.selectedItem.toString()
            val durum = binding.spDurum.selectedItem.toString()
            val yildiz = binding.ratingBar.rating
            val puan = "${yildiz.toInt()}/5"
            val kimeAit = auth.currentUser?.email ?: "Anonim"

            if (ad.isNotEmpty()) {
                val yeniOyun = Oyun("", kimeAit, ad, platform, durum, puan)

                db.collection("oyunlar").add(yeniOyun)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Oyun Buluta Kaydedildi!", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Hata: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(this, "Oyun adı giriniz", Toast.LENGTH_SHORT).show()
            }
        }
    }
}