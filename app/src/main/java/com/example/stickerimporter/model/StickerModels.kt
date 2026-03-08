package com.example.stickerimporter.model

data class StickerDescriptor(
    val fileName: String,
    val emojis: List<String> = emptyList()
)

data class StickerPack(
    val identifier: String,
    val name: String,
    val publisher: String,
    val trayImageFile: String,
    val stickers: List<StickerDescriptor>,
    val imageDataVersion: String = "1",
    val avoidCache: Boolean = false,
    val publisherEmail: String = "",
    val publisherWebsite: String = "",
    val privacyPolicyWebsite: String = "",
    val licenseAgreementWebsite: String = ""
)
