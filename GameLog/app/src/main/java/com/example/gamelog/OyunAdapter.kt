package com.example.gamelog

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth // KİMLİK KONTROLÜ İÇİN GEREKLİ

class OyunAdapter(
    private val oyunListesi: ArrayList<Oyun>,
    private val tiklamaOlayi: (Oyun) -> Unit
) : RecyclerView.Adapter<OyunAdapter.OyunViewHolder>() {

    // Mevcut kullanıcının emailini alıyoruz
    private val guncelKullaniciEmail = FirebaseAuth.getInstance().currentUser?.email

    class OyunViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvAd: TextView = itemView.findViewById(R.id.tvListeAd)
        val tvPlatform: TextView = itemView.findViewById(R.id.tvListePlatform)
        val tvDurum: TextView = itemView.findViewById(R.id.tvListeDurum)
        val tvPuan: TextView = itemView.findViewById(R.id.tvListePuan)
        val ivKapak: ImageView = itemView.findViewById(R.id.ivOyunKapak)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OyunViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_oyun, parent, false)
        return OyunViewHolder(view)
    }

    override fun onBindViewHolder(holder: OyunViewHolder, position: Int) {
        val oyun = oyunListesi[position]

        holder.tvAd.text = oyun.oyunAdi
        holder.tvPlatform.text = oyun.platform

        // --- ÖNEMLİ DEĞİŞİKLİK BURADA ---
        // Eğer oyun BENİM değilse (Admin'in veya başkasınınsa), durumu ve puanı GİZLE.
        if (oyun.kimeAit != guncelKullaniciEmail) {
            holder.tvDurum.visibility = View.GONE  // Yazıyı görünmez yap
            holder.tvPuan.visibility = View.GONE   // Puanı görünmez yap
            // İstersen buraya "Listeye Ekle" ikonu vb. koyabilirsin ama şimdilik boş kalsın.
        }
        else {
            // Oyun BENİM ise her şeyi göster
            holder.tvDurum.visibility = View.VISIBLE
            holder.tvPuan.visibility = View.VISIBLE

            holder.tvDurum.text = oyun.durum
            holder.tvPuan.text = if (oyun.puan.isNotEmpty()) "${oyun.puan} ⭐" else ""

            // Rengi ayarla
            when (oyun.durum) {
                "Bitirdim" -> holder.tvDurum.setTextColor(Color.parseColor("#00FF7F"))
                "Oynuyorum" -> holder.tvDurum.setTextColor(Color.parseColor("#00BFFF"))
                "İstek Listesi" -> holder.tvDurum.setTextColor(Color.parseColor("#FFD700"))
                "Bıraktım" -> holder.tvDurum.setTextColor(Color.parseColor("#FF4500"))
                else -> holder.tvDurum.setTextColor(Color.GRAY)
            }
        }
        // -------------------------------

        // Glide ile Resim Yükleme (Burası aynı)
        if (oyun.resimUrl.isNotEmpty()) {
            Glide.with(holder.itemView.context)
                .load(oyun.resimUrl)
                .centerCrop()
                .placeholder(android.R.drawable.ic_menu_gallery)
                .into(holder.ivKapak)
        } else {
            holder.ivKapak.setImageResource(android.R.drawable.ic_menu_gallery)
        }

        holder.itemView.setOnClickListener {
            tiklamaOlayi(oyun)
        }
    }

    override fun getItemCount(): Int {
        return oyunListesi.size
    }
}