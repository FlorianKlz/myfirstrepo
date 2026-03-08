package com.mystickers.whatsapp

import android.os.Parcel
import android.os.Parcelable

/**
 * Represents a single sticker within a sticker pack.
 */
data class Sticker(
    val imageFileName: String,
    val emojis: List<String>,
    val size: Long = 0
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.createStringArrayList() ?: emptyList(),
        parcel.readLong()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(imageFileName)
        parcel.writeStringList(emojis)
        parcel.writeLong(size)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<Sticker> {
        override fun createFromParcel(parcel: Parcel): Sticker = Sticker(parcel)
        override fun newArray(size: Int): Array<Sticker?> = arrayOfNulls(size)
    }
}
