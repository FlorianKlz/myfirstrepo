package com.whatsapp.stickerimporter;

import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;

/**
 * Activity to display details of a sticker pack
 */
public class StickerPackDetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sticker_pack_details);

        StickerPack stickerPack = getIntent().getParcelableExtra("sticker_pack");
        if (stickerPack == null) {
            finish();
            return;
        }

        // Set up action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(stickerPack.name);
        }

        // Display pack info
        TextView packName = findViewById(R.id.packName);
        TextView packInfo = findViewById(R.id.packInfo);
        GridLayout stickerGrid = findViewById(R.id.stickerGrid);

        packName.setText(stickerPack.name);
        packInfo.setText(stickerPack.stickers.size() + " stickers\n" +
                        "Publisher: " + stickerPack.publisher);

        // Display all stickers in grid
        File packDir = new File(getFilesDir(), "sticker_packs/" + stickerPack.identifier);

        for (Sticker sticker : stickerPack.stickers) {
            ImageView imageView = new ImageView(this);
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 200;
            params.height = 200;
            params.setMargins(8, 8, 8, 8);
            imageView.setLayoutParams(params);
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);

            File stickerFile = new File(packDir, sticker.imageFileName);
            if (stickerFile.exists()) {
                imageView.setImageBitmap(BitmapFactory.decodeFile(stickerFile.getAbsolutePath()));
            }

            stickerGrid.addView(imageView);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
