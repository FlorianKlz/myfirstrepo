package com.whatsapp.stickerimporter;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for displaying sticker packs in RecyclerView
 */
public class StickerPackAdapter extends RecyclerView.Adapter<StickerPackAdapter.ViewHolder> {

    private final Context context;
    private List<StickerPack> stickerPacks;
    private final OnStickerPackClickListener listener;

    public interface OnStickerPackClickListener {
        void onAddToWhatsApp(StickerPack stickerPack);
        void onDelete(StickerPack stickerPack);
        void onViewDetails(StickerPack stickerPack);
    }

    public StickerPackAdapter(Context context, List<StickerPack> stickerPacks, OnStickerPackClickListener listener) {
        this.context = context;
        this.stickerPacks = new ArrayList<>(stickerPacks);
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_sticker_pack, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        StickerPack stickerPack = stickerPacks.get(position);

        holder.packName.setText(stickerPack.name);
        holder.packInfo.setText(stickerPack.stickers.size() + " stickers • " +
            formatFileSize(stickerPack.totalSize));

        // Load tray icon
        File packDir = new File(context.getFilesDir(), "sticker_packs/" + stickerPack.identifier);
        File trayFile = new File(packDir, stickerPack.trayImageFile);
        if (trayFile.exists()) {
            holder.trayIcon.setImageBitmap(BitmapFactory.decodeFile(trayFile.getAbsolutePath()));
        } else {
            holder.trayIcon.setImageResource(R.drawable.ic_launcher);
        }

        holder.addButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onAddToWhatsApp(stickerPack);
            }
        });

        holder.deleteButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDelete(stickerPack);
            }
        });

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onViewDetails(stickerPack);
            }
        });
    }

    @Override
    public int getItemCount() {
        return stickerPacks.size();
    }

    public void updateStickerPacks(List<StickerPack> newPacks) {
        this.stickerPacks = new ArrayList<>(newPacks);
        notifyDataSetChanged();
    }

    private String formatFileSize(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.1f KB", bytes / 1024.0);
        } else {
            return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView trayIcon;
        TextView packName;
        TextView packInfo;
        Button addButton;
        Button deleteButton;

        ViewHolder(View itemView) {
            super(itemView);
            trayIcon = itemView.findViewById(R.id.trayIcon);
            packName = itemView.findViewById(R.id.packName);
            packInfo = itemView.findViewById(R.id.packInfo);
            addButton = itemView.findViewById(R.id.addButton);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }
    }
}
