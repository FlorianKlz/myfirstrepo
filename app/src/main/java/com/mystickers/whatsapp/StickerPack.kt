package com.mystickers.whatsapp

import android.os.Parcel
import android.os.Parcelable

/**
 * Represents a sticker pack that can be added to WhatsApp.
 * WhatsApp supports a maximum of 30 stickers per pack.
 */
data class StickerPack(
    val identifier: String,
    val name: String,
    val publisher: String,
    val trayImageFile: String,
    val publisherEmail: String = "",
    val publisherWebsite: String = "",
    val privacyPolicyWebsite: String = "",
    val licenseAgreementWebsite: String = "",
    val imageDataVersion: String = "1",
    val avoidCache: Boolean = false,
    val animatedStickerPack: Boolean = false,
    val stickers: MutableList<Sticker> = mutableListOf(),
    var isWhatsAppInstalled: Boolean = false
) : Parcelable {

    val totalSize: Long
        get() = stickers.sumOf { it.size }

    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "1",
        parcel.readByte() != 0.toByte(),
        parcel.readByte() != 0.toByte(),
        mutableListOf<Sticker>().apply { parcel.readTypedList(this, Sticker.CREATOR) },
        parcel.readByte() != 0.toByte()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(identifier)
        parcel.writeString(name)
        parcel.writeString(publisher)
        parcel.writeString(trayImageFile)
        parcel.writeString(publisherEmail)
        parcel.writeString(publisherWebsite)
        parcel.writeString(privacyPolicyWebsite)
        parcel.writeString(licenseAgreementWebsite)
        parcel.writeString(imageDataVersion)
        parcel.writeByte(if (avoidCache) 1 else 0)
        parcel.writeByte(if (animatedStickerPack) 1 else 0)
        parcel.writeTypedList(stickers)
        parcel.writeByte(if (isWhatsAppInstalled) 1 else 0)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<StickerPack> {
        const val MAX_STICKERS_PER_PACK = 30

        override fun createFromParcel(parcel: Parcel): StickerPack = StickerPack(parcel)
        override fun newArray(size: Int): Array<StickerPack?> = arrayOfNulls(size)
    }
}
