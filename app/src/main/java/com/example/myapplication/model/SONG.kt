package com.example.myapplication.model

import android.os.Parcel
import android.os.Parcelable

class SONG(

    private val id: Long,
    val title: String?,
    val artist: String?,
    val duration: Long,
    val data: String?,
    val image: String?
): Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readLong(),
        parcel.readString(),
        parcel.readString(),
        parcel.readLong(),
        parcel.readString(),
        parcel.readString()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id)
        parcel.writeString(title)
        parcel.writeString(artist)
        parcel.writeLong(duration)
        parcel.writeString(data)
        parcel.writeString(image)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<SONG> {
        override fun createFromParcel(parcel: Parcel): SONG {
            return SONG(parcel)
        }

        override fun newArray(size: Int): Array<SONG?> {
            return arrayOfNulls(size)
        }
    }
}