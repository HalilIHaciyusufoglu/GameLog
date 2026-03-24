package com.example.gamelog

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.gamelog.databinding.ActivityProfilBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.io.ByteArrayOutputStream

class ProfilActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfilBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    // Senin yazdığın galeri başlatıcısı
    private val galeriyeGit = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            // Ekranda göster (Padding'i kaldır ki tam otursun)
            binding.ivProfilResmi.setPadding(0,0,0,0)
            binding.ivProfilResmi.setImageURI(uri)
            // Veritabanına kaydet
            resmiVeritabaninaKaydet(uri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfilBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Verileri Yükle
        kullaniciBilgileriniGetir()

        // Resme Tıklayınca Galeri Aç
        binding.ivProfilResmi.setOnClickListener {
            galeriyeGit.launch("image/*")
        }

        // Çıkış Yap
        binding.btnCikisYap.setOnClickListener {
            auth.signOut()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    private fun kullaniciBilgileriniGetir() {
        val uid = auth.currentUser?.uid ?: return

        db.collection("users").document(uid).get()
            .addOnSuccessListener { belge ->
                val user = belge.toObject(User::class.java)
                if (user != null) {
                    binding.tvProfilAd.text = user.kullaniciAdi ?: "Kullanıcı"
                    binding.tvProfilEmail.text = user.email

                    // Resim varsa çözümle ve göster (Senin kodun)
                    if (user.profilResmi.isNotEmpty()) {
                        try {
                            val imageBytes = Base64.decode(user.profilResmi, Base64.DEFAULT)
                            val decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                            binding.ivProfilResmi.setPadding(0,0,0,0) // Varsayılan padding'i kaldır
                            binding.ivProfilResmi.setImageBitmap(decodedImage)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }

                    // İstatistikleri de getir
                    istatistikleriGetir(user.email)
                }
            }
    }

    private fun istatistikleriGetir(email: String) {
        // Yeni tasarıma göre 4 veriyi de sayıyoruz
        db.collection("oyunlar").whereEqualTo("kimeAit", email).addSnapshotListener { docs, e ->
            if (e != null || docs == null) return@addSnapshotListener

            var toplam = 0
            var biten = 0
            var oynuyorum = 0
            var istek = 0

            for (doc in docs) {
                toplam++
                val durum = doc.getString("durum")
                if (durum == "Bitirdim") biten++
                if (durum == "Oynuyorum") oynuyorum++
                if (durum == "İstek Listesi") istek++
            }

            // Yeni kutucuklara yazdır
            binding.tvSayiToplam.text = toplam.toString()
            binding.tvSayiBitirdim.text = biten.toString()
            binding.tvSayiOynuyorum.text = oynuyorum.toString()
            binding.tvSayiIstek.text = istek.toString()
        }
    }

    // Senin yazdığın resim sıkıştırma ve kaydetme kodu (Aynen korundu)
    private fun resmiVeritabaninaKaydet(uri: Uri) {
        val uid = auth.currentUser?.uid ?: return
        try {
            val bitmap = if (Build.VERSION.SDK_INT >= 28) {
                val source = ImageDecoder.createSource(contentResolver, uri)
                ImageDecoder.decodeBitmap(source)
            } else {
                MediaStore.Images.Media.getBitmap(contentResolver, uri)
            }

            val kucukBitmap = Bitmap.createScaledBitmap(bitmap, 300, 300, true)

            val baos = ByteArrayOutputStream()
            kucukBitmap.compress(Bitmap.CompressFormat.JPEG, 60, baos)
            val resimString = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT)

            db.collection("users").document(uid).update("profilResmi", resimString)
                .addOnSuccessListener {
                    Toast.makeText(this, "Profil resmi güncellendi!", Toast.LENGTH_SHORT).show()
                }

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Resim yüklenirken hata oluştu", Toast.LENGTH_SHORT).show()
        }
    }
}