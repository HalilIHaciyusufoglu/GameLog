package com.example.gamelog

import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore

class SansliActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var ivResim: ImageView
    private lateinit var tvAd: TextView
    private lateinit var tvPlatform: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sansli)

        db = FirebaseFirestore.getInstance()
        ivResim = findViewById(R.id.ivSansliResim)
        tvAd = findViewById(R.id.tvSansliAd)
        tvPlatform = findViewById(R.id.tvSansliPlatform)

        rastgeleOyunGetir()

        findViewById<Button>(R.id.btnTekrarDene).setOnClickListener {
            rastgeleOyunGetir()
        }

        findViewById<Button>(R.id.btnSansliKapat).setOnClickListener {
            finish()
        }
    }

    private fun rastgeleOyunGetir() {
        tvAd.text = "Zar atılıyor... 🎲"

        db.collection("oyunlar").get().addOnSuccessListener { documents ->
            if (!documents.isEmpty) {
                // Listeden rastgele bir sayı seç
                val randomIndex = (0 until documents.size()).random()
                val secilenBelge = documents.documents[randomIndex]
                val oyun = secilenBelge.toObject(Oyun::class.java)

                if (oyun != null) {
                    tvAd.text = oyun.oyunAdi
                    tvPlatform.text = oyun.platform

                    if (oyun.resimUrl.isNotEmpty()) {
                        Glide.with(this).load(oyun.resimUrl).centerCrop().into(ivResim)
                    }
                }
            } else {
                tvAd.text = "Hiç oyun yok!"
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Hata oluştu!", Toast.LENGTH_SHORT).show()
        }
    }
}