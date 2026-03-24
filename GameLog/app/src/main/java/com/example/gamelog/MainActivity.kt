package com.example.gamelog

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gamelog.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    private val oyunListesi = ArrayList<Oyun>()
    private val tumOyunlarYedek = ArrayList<Oyun>()
    private lateinit var adapter: OyunAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        val isAdmin = intent.getBooleanExtra("is_admin", false)

        // Hoşgeldin Mesajı
        val uid = auth.currentUser?.uid
        if (uid != null) {
            db.collection("users").document(uid).get().addOnSuccessListener {
                val user = it.toObject(User::class.java)
                val isim = user?.kullaniciAdi ?: "Oyuncu"
                binding.tvHosgeldin.text = "Hoşgeldin,\n$isim ${if(isAdmin) "(ADMIN)" else ""} 👋"
            }
        }

        binding.fabEkle.visibility = if (isAdmin) View.VISIBLE else View.GONE

        binding.btnProfil.setOnClickListener { startActivity(Intent(this, ProfilActivity::class.java)) }
        binding.fabEkle.setOnClickListener { startActivity(Intent(this, EkleActivity::class.java)) }
        binding.btnSansli.setOnClickListener { startActivity(Intent(this, SansliActivity::class.java)) }

        adapter = OyunAdapter(oyunListesi) { secilenOyun ->
            val intent = Intent(this, DetayActivity::class.java)
            intent.putExtra("id", secilenOyun.id)
            intent.putExtra("ad", secilenOyun.oyunAdi)
            intent.putExtra("platform", secilenOyun.platform)
            intent.putExtra("durum", secilenOyun.durum)
            intent.putExtra("puan", secilenOyun.puan)
            intent.putExtra("kimeAit", secilenOyun.kimeAit)
            intent.putExtra("hikaye", secilenOyun.hikaye)
            intent.putExtra("resimUrl", secilenOyun.resimUrl)
            startActivity(intent)
        }

        binding.rvOyunlar.layoutManager = LinearLayoutManager(this)
        binding.rvOyunlar.adapter = adapter

        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(r: RecyclerView, v: RecyclerView.ViewHolder, t: RecyclerView.ViewHolder): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val silinecekOyun = oyunListesi[position]
                val myEmail = auth.currentUser?.email

                if (isAdmin || silinecekOyun.kimeAit == myEmail) {
                    db.collection("oyunlar").document(silinecekOyun.id).delete()
                        .addOnSuccessListener {
                            Toast.makeText(this@MainActivity, "Oyun silindi 🗑️", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(this@MainActivity, "Sadece kendi oyunlarını silebilirsin!", Toast.LENGTH_SHORT).show()
                    adapter.notifyItemChanged(position)
                }
            }
        })
        itemTouchHelper.attachToRecyclerView(binding.rvOyunlar)

        verileriDinle()
        filtreButonlariniAyarla()
        aramaCubugunuAyarla()
    }

    private fun verileriDinle() {
        db.collection("oyunlar").orderBy("oyunAdi", Query.Direction.ASCENDING)
            .addSnapshotListener { value, error ->
                if (error != null || value == null) return@addSnapshotListener
                tumOyunlarYedek.clear()
                for (doc in value.documents) {
                    val oyun = doc.toObject(Oyun::class.java)
                    if (oyun != null) {
                        oyun.id = doc.id
                        tumOyunlarYedek.add(oyun)
                    }
                }
                // Varsayılan olarak tüm oyunları göster
                listeyiGuncelle(tumOyunlarYedek)
            }
    }

    private fun aramaCubugunuAyarla() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false
            override fun onQueryTextChange(newText: String?): Boolean {
                val aranan = newText?.lowercase() ?: ""
                listeyiGuncelle(tumOyunlarYedek.filter { it.oyunAdi.lowercase().contains(aranan) })
                return true
            }
        })
    }

    private fun filtreButonlariniAyarla() {
        val email = auth.currentUser?.email

        binding.btnFiltreTumu.setOnClickListener {
            listeyiGuncelle(tumOyunlarYedek)
            renkGuncelle(binding.btnFiltreTumu)
        }

        binding.btnFiltreBenim.setOnClickListener {
            listeyiGuncelle(tumOyunlarYedek.filter { it.kimeAit == email })
            renkGuncelle(binding.btnFiltreBenim)
        }

        binding.btnFiltreIstek.setOnClickListener {
            listeyiGuncelle(tumOyunlarYedek.filter { it.kimeAit == email && it.durum == "İstek Listesi" })
            renkGuncelle(binding.btnFiltreIstek)
        }

        binding.btnFiltreBitirdim.setOnClickListener {
            listeyiGuncelle(tumOyunlarYedek.filter { it.kimeAit == email && it.durum == "Bitirdim" })
            renkGuncelle(binding.btnFiltreBitirdim)
        }
    }

    private fun listeyiGuncelle(yeniListe: List<Oyun>) {
        oyunListesi.clear()
        oyunListesi.addAll(yeniListe)
        adapter.notifyDataSetChanged()
    }

    private fun renkGuncelle(aktifBtn: Button) {
        val butonlar = listOf(binding.btnFiltreTumu, binding.btnFiltreBenim, binding.btnFiltreIstek, binding.btnFiltreBitirdim)
        for (btn in butonlar) {
            if (btn == aktifBtn) {
                btn.setBackgroundColor(Color.parseColor("#00BFFF"))
                btn.setTextColor(Color.WHITE)
            } else {
                btn.setBackgroundColor(Color.parseColor("#333333"))
                btn.setTextColor(Color.LTGRAY)
            }
        }
    }
}