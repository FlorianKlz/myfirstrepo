package com.mystickers.whatsapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import android.graphics.BitmapFactory

/**
 * RecyclerView adapter for displaying sticker packs.
 */
class StickerPackAdapter(
    private val packs: List<StickerPack>,
    private val onAddToWhatsApp: (StickerPack) -> Unit
) : RecyclerView.Adapter<StickerPackAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val trayIcon: ImageView = view.findViewById(R.id.tray_icon)
        val packName: TextView = view.findViewById(R.id.pack_name)
        val packInfo: TextView = view.findViewById(R.id.pack_info)
        val addButton: Button = view.findViewById(R.id.btn_add_to_whatsapp)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_sticker_pack, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val pack = packs[position]
        holder.packName.text = pack.name
        holder.packInfo.text = "${pack.stickers.size} Sticker · ${pack.publisher}"

        // Load tray icon
        val context = holder.itemView.context
        val manager = StickerPackManager.getInstance(context)
        val trayFile = manager.getTrayIconFile(pack.identifier)
        if (trayFile != null && trayFile.exists()) {
            val bitmap = BitmapFactory.decodeFile(trayFile.absolutePath)
            holder.trayIcon.setImageBitmap(bitmap)
        }

        holder.addButton.setOnClickListener {
            onAddToWhatsApp(pack)
        }
    }

    override fun getItemCount(): Int = packs.size
}
