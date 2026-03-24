package com.example.gamelog

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class DetayActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detay)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        // Verileri Al
        val id = intent.getStringExtra("id") ?: ""
        val ad = intent.getStringExtra("ad") ?: ""
        val platform = intent.getStringExtra("platform") ?: ""
        var durum = intent.getStringExtra("durum") ?: ""
        var puan = intent.getStringExtra("puan") ?: "0/5"
        val kimeAit = intent.getStringExtra("kimeAit") ?: ""
        val hikaye = intent.getStringExtra("hikaye") ?: "Hikaye yok."
        val resimUrl = intent.getStringExtra("resimUrl") ?: ""

        val currentUserEmail = auth.currentUser?.email

        // Elemanları Tanımla
        val ivResim = findViewById<ImageView>(R.id.ivDetayResim)
        val tvAd = findViewById<TextView>(R.id.tvDetayAd)
        val tvPlatform = findViewById<TextView>(R.id.tvDetayPlatform)
        val tvEkleyen = findViewById<TextView>(R.id.tvDetayEkleyen)
        val tvDurum = findViewById<TextView>(R.id.tvDetayDurum)
        val tvHikaye = findViewById<TextView>(R.id.tvDetayHikaye)
        val ratingMain = findViewById<RatingBar>(R.id.ratingDetay)

        val layoutKisisel = findViewById<LinearLayout>(R.id.layoutKisiselBilgiler)
        val btnListemeEkle = findViewById<Button>(R.id.btnListemeEkle)
        val btnSil = findViewById<Button>(R.id.btnSil)
        val btnDuzenle = findViewById<Button>(R.id.btnDuzenle)
        val btnGeri = findViewById<ImageButton>(R.id.btnGeri)

        // Verileri Yerleştir
        tvAd.text = ad
        tvPlatform.text = "Platform: $platform"
        tvEkleyen.text = "Ekleyen: ${if (kimeAit == currentUserEmail) "Sen" else kimeAit}"
        tvDurum.text = "Durum: $durum"
        tvHikaye.text = hikaye

        if (resimUrl.isNotEmpty()) {
            Glide.with(this).load(resimUrl).into(ivResim)
        }

        // Ana ekrandaki yıldızları ayarla
        try {
            val yildiz = puan.split("/")[0].toFloat()
            ratingMain.rating = yildiz
        } catch (e: Exception) { ratingMain.rating = 0f }

        // Yetki Kontrolü
        if (kimeAit == currentUserEmail) {
            layoutKisisel.visibility = View.VISIBLE
            btnListemeEkle.visibility = View.GONE
        } else {
            layoutKisisel.visibility = View.GONE
            btnListemeEkle.visibility = View.VISIBLE
        }

        // --- BUTON İŞLEMLERİ ---

        btnGeri.setOnClickListener { finish() }

        btnSil.setOnClickListener {
            db.collection("oyunlar").document(id).delete().addOnSuccessListener {
                Toast.makeText(this, "Oyun silindi!", Toast.LENGTH_SHORT).show()
                finish()
            }
        }

        btnListemeEkle.setOnClickListener {
            if (currentUserEmail != null) {
                // İstek listesine ekleme sorunu: Zaten var mı kontrolü yapılabilir ama
                // burada basitçe ekliyoruz. Duplicate kontrolünü MainActivity'de yapacağız.
                val yeniOyun = hashMapOf(
                    "kimeAit" to currentUserEmail,
                    "oyunAdi" to ad,
                    "platform" to platform,
                    "durum" to "İstek Listesi",
                    "puan" to "0/5",
                    "hikaye" to hikaye,
                    "resimUrl" to resimUrl
                )
                db.collection("oyunlar").add(yeniOyun).addOnSuccessListener {
                    Toast.makeText(this, "Listene eklendi! 🎉", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }

        // --- DÜZENLEME EKRANI (XML'den Yükleniyor) ---
        btnDuzenle.setOnClickListener {
            val builder = AlertDialog.Builder(this)

            // XML tasarımını yükle
            val view = layoutInflater.inflate(R.layout.dialog_duzenle, null)
            builder.setView(view)
            val dialog = builder.create()

            // XML içindeki elemanları bul
            val spDurum = view.findViewById<Spinner>(R.id.spDuzenleDurum)
            val ratingDuzenle = view.findViewById<RatingBar>(R.id.ratingDuzenle)
            val btnGuncelle = view.findViewById<Button>(R.id.btnGuncelle)

            // Spinner Seçeneklerini Ayarla
            val durumlar = arrayOf("İstek Listesi", "Oynuyorum", "Bitirdim", "Yarım Bıraktım")
            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, durumlar)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spDurum.adapter = adapter

            // Mevcut durumu seçili hale getir
            val mevcutDurumIndex = durumlar.indexOf(durum)
            if (mevcutDurumIndex >= 0) {
                spDurum.setSelection(mevcutDurumIndex)
            }

            // Mevcut puanı rating bar'a getir
            try {
                ratingDuzenle.rating = puan.split("/")[0].toFloat()
            } catch (e: Exception) {}

            btnGuncelle.setOnClickListener {
                val yeniDurum = spDurum.selectedItem.toString()
                val yeniPuan = "${ratingDuzenle.rating.toInt()}/5"

                // Veritabanını Güncelle
                db.collection("oyunlar").document(id).update(
                    mapOf(
                        "durum" to yeniDurum,
                        "puan" to yeniPuan
                    )
                ).addOnSuccessListener {
                    Toast.makeText(this, "Güncellendi! ✅", Toast.LENGTH_SHORT).show()

                    // Ekranı güncelle
                    tvDurum.text = "Durum: $yeniDurum"
                    ratingMain.rating = ratingDuzenle.rating

                    // Değişkenleri güncelle ki bir daha açılırsa doğru gelsin
                    durum = yeniDurum
                    puan = yeniPuan

                    dialog.dismiss()
                }
            }

            dialog.show()
        }
    }
}