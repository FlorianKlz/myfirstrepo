# WhatsApp Sticker Importer

Eine Android-App zum Importieren eigener WebP-Sticker in WhatsApp.

## Features

- **Massenimport**: Importiere Tausende von WebP-Stickern auf einmal
- **Automatische Paketierung**: Sticker werden automatisch in WhatsApp-kompatible Pakete mit je 30 Stickern aufgeteilt
- **Bildoptimierung**: Sticker werden automatisch auf 512×512 Pixel skaliert und auf unter 100 KB komprimiert
- **Tray-Icons**: Automatische Generierung von Paket-Vorschaubildern (96×96 Pixel)
- **Einfache Bedienung**: Ordner auswählen → Sticker werden verarbeitet → Zu WhatsApp hinzufügen
- **Alle auf einmal**: Möglichkeit, alle Pakete nacheinander zu WhatsApp hinzuzufügen

## Voraussetzungen

- Android 7.0 (API 24) oder höher
- WhatsApp installiert
- WebP-Stickerdateien im Gerätespeicher

## Installation & Build

### Mit Android Studio

1. Repository klonen
2. In Android Studio öffnen
3. Gradle Sync durchführen
4. Auf Gerät/Emulator installieren (Run → Run 'app')

### Über die Kommandozeile

```bash
./gradlew assembleDebug
adb install app/build/outputs/apk/debug/app-debug.apk
```

## Verwendung

1. **App öffnen** – Beim ersten Start werden Speicherberechtigungen angefragt
2. **Ordner auswählen** – Tippe auf „Sticker-Ordner auswählen" und wähle den Ordner mit deinen WebP-Stickern
3. **Verarbeitung abwarten** – Die App scannt den Ordner, verarbeitet die Bilder und erstellt Sticker-Pakete
4. **Zu WhatsApp hinzufügen** – Tippe auf „Hinzufügen" bei einem einzelnen Paket oder „Alle Pakete zu WhatsApp hinzufügen"
5. **In WhatsApp bestätigen** – Jedes Paket muss in WhatsApp einzeln bestätigt werden

## Sticker-Anforderungen

| Eigenschaft | Anforderung |
|---|---|
| Format | WebP |
| Sticker-Größe | 512 × 512 Pixel (wird automatisch angepasst) |
| Maximale Dateigröße | 100 KB pro Sticker (wird automatisch komprimiert) |
| Sticker pro Paket | Maximal 30 (WhatsApp-Limit) |
| Tray-Icon | 96 × 96 Pixel, max. 50 KB (wird automatisch generiert) |

## Projektstruktur

```
app/src/main/java/com/mystickers/whatsapp/
├── MainActivity.kt           # Hauptbildschirm mit Ordnerauswahl und Paketliste
├── StickerContentProvider.kt  # ContentProvider für die WhatsApp Sticker API
├── StickerPack.kt            # Datenmodell für ein Sticker-Paket
├── Sticker.kt                # Datenmodell für einen einzelnen Sticker
├── StickerPackManager.kt     # Laden, Verarbeiten und Verwalten der Sticker-Pakete
├── StickerPackAdapter.kt     # RecyclerView-Adapter für die Paketliste
├── ImageUtil.kt              # Bildverarbeitung (Skalierung, Komprimierung)
└── WhatsAppIntentHelper.kt   # WhatsApp-Integration (Intents)
```

## Technische Details

### WhatsApp Sticker API

Die App verwendet die offizielle WhatsApp Sticker API:
- Ein `ContentProvider` stellt die Sticker-Pakete über definierte URI-Pfade bereit
- WhatsApp fragt den ContentProvider ab, um Paketinformationen und Stickerdaten zu erhalten
- Das Hinzufügen erfolgt über einen Intent (`com.whatsapp.intent.action.ENABLE_STICKER_PACK`)

### Verarbeitung von ~2000 Stickern

Bei 2000 WebP-Dateien erstellt die App automatisch ca. 67 Sticker-Pakete (2000 ÷ 30).
Jedes Paket enthält bis zu 30 Sticker und ein automatisch generiertes Tray-Icon.

## Lizenz

Dieses Projekt ist Open Source.
