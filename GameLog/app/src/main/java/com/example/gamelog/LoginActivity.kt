package com.example.gamelog

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.gamelog.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        if (auth.currentUser != null) {
            anaSayfayaGit()
        }

        // ŞİFREMİ UNUTTUM
        binding.tvSifreUnuttum.setOnClickListener {
            val email = binding.etKullaniciAdi.text.toString().trim()
            if (email.isNotEmpty()) {
                auth.sendPasswordResetEmail(email)
                    .addOnSuccessListener { Toast.makeText(this, "Sıfırlama maili atıldı!", Toast.LENGTH_LONG).show() }
                    .addOnFailureListener { Toast.makeText(this, "Hata: ${it.localizedMessage}", Toast.LENGTH_SHORT).show() }
            } else {
                Toast.makeText(this, "E-posta giriniz", Toast.LENGTH_SHORT).show()
            }
        }

        // KAYIT OL
        binding.btnKayit.setOnClickListener {
            val email = binding.etKullaniciAdi.text.toString().trim()
            val sifre = binding.etSifre.text.toString().trim()
            val nick = binding.etKullaniciNick.text.toString().trim() // Yeni alan

            if (email.isNotEmpty() && sifre.isNotEmpty() && nick.isNotEmpty()) {
                auth.createUserWithEmailAndPassword(email, sifre)
                    .addOnSuccessListener { gorev ->
                        val isAdmin = email.contains("admin")
                        // Yeni User yapısı: Email, Nick, Resim(boş), Admin
                        val yeniUser = User(email, nick, "", isAdmin)

                        db.collection("users").document(gorev.user!!.uid).set(yeniUser)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Kayıt Başarılı!", Toast.LENGTH_SHORT).show()
                                anaSayfayaGit()
                            }
                    }
                    .addOnFailureListener { hata ->
                        Toast.makeText(this, "Hata: ${hata.localizedMessage}", Toast.LENGTH_LONG).show()
                    }
            } else {
                Toast.makeText(this, "Tüm alanları doldurun (Kullanıcı adı dahil)", Toast.LENGTH_SHORT).show()
            }
        }

        // GİRİŞ YAP
        binding.btnGiris.setOnClickListener {
            val email = binding.etKullaniciAdi.text.toString().trim()
            val sifre = binding.etSifre.text.toString().trim()

            if (email.isNotEmpty() && sifre.isNotEmpty()) {
                auth.signInWithEmailAndPassword(email, sifre)
                    .addOnSuccessListener { anaSayfayaGit() }
                    .addOnFailureListener { Toast.makeText(this, "Hata: ${it.localizedMessage}", Toast.LENGTH_LONG).show() }
            } else {
                Toast.makeText(this, "Bilgileri giriniz", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun anaSayfayaGit() {
        val uid = auth.currentUser?.uid
        if (uid != null) {
            db.collection("users").document(uid).get()
                .addOnSuccessListener { belge ->
                    if (belge.exists()) {
                        val user = belge.toObject(User::class.java)
                        // Ana Sayfaya sadece admin bilgisini gönderelim, gerisini orada çekeriz
                        val intent = Intent(this, MainActivity::class.java)
                        intent.putExtra("is_admin", user?.isAdmin)
                        startActivity(intent)
                        finish()
                    }
                }
        }
    }
}