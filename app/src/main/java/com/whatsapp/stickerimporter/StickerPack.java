package com.whatsapp.stickerimporter;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a sticker pack containing multiple stickers
 */
public class StickerPack implements Parcelable {
    public String identifier;
    public String name;
    public String publisher;
    public String trayImageFile;
    public String publisherEmail;
    public String publisherWebsite;
    public String privacyPolicyWebsite;
    public String licenseAgreementWebsite;
    public String imageDataVersion;
    public boolean avoidCache;
    public long totalSize;
    public List<Sticker> stickers;
    public boolean isWhitelisted;

    public StickerPack(String identifier, String name, String publisher, String trayImageFile,
                       String publisherEmail, String publisherWebsite, String privacyPolicyWebsite,
                       String licenseAgreementWebsite) {
        this.identifier = identifier;
        this.name = name;
        this.publisher = publisher;
        this.trayImageFile = trayImageFile;
        this.publisherEmail = publisherEmail;
        this.publisherWebsite = publisherWebsite;
        this.privacyPolicyWebsite = privacyPolicyWebsite;
        this.licenseAgreementWebsite = licenseAgreementWebsite;
        this.stickers = new ArrayList<>();
        this.totalSize = 0;
    }

    protected StickerPack(Parcel in) {
        identifier = in.readString();
        name = in.readString();
        publisher = in.readString();
        trayImageFile = in.readString();
        publisherEmail = in.readString();
        publisherWebsite = in.readString();
        privacyPolicyWebsite = in.readString();
        licenseAgreementWebsite = in.readString();
        imageDataVersion = in.readString();
        avoidCache = in.readByte() != 0;
        totalSize = in.readLong();
        stickers = in.createTypedArrayList(Sticker.CREATOR);
        isWhitelisted = in.readByte() != 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(identifier);
        dest.writeString(name);
        dest.writeString(publisher);
        dest.writeString(trayImageFile);
        dest.writeString(publisherEmail);
        dest.writeString(publisherWebsite);
        dest.writeString(privacyPolicyWebsite);
        dest.writeString(licenseAgreementWebsite);
        dest.writeString(imageDataVersion);
        dest.writeByte((byte) (avoidCache ? 1 : 0));
        dest.writeLong(totalSize);
        dest.writeTypedList(stickers);
        dest.writeByte((byte) (isWhitelisted ? 1 : 0));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<StickerPack> CREATOR = new Creator<StickerPack>() {
        @Override
        public StickerPack createFromParcel(Parcel in) {
            return new StickerPack(in);
        }

        @Override
        public StickerPack[] newArray(int size) {
            return new StickerPack[size];
        }
    };

    public void setAndroidPlayStoreLink(String androidPlayStoreLink) {
        // Reserved for future use
    }

    public void setIosAppStoreLink(String iosAppStoreLink) {
        // Reserved for future use
    }
}
