# WhatsApp Sticker Importer

Eine Android-Anwendung zum Importieren von WhatsApp-Stickern aus WebP-Dateien auf Ihrem Gerät.

## Übersicht

Diese App ermöglicht es Ihnen, bis zu 2000 WebP-Sticker-Dateien von Ihrem Gerät zu scannen und in WhatsApp zu importieren. Die App organisiert die Sticker automatisch in Pakete mit jeweils bis zu 30 Stickern, wie es WhatsApp erfordert.

## Features

- **Automatisches Scannen**: Scannt Ihr Gerät nach WebP-Dateien
- **Intelligente Organisation**: Erstellt automatisch Stickerpakete mit bis zu 30 Stickern pro Paket
- **Größenoptimierung**: Komprimiert automatisch zu große Dateien (>100KB)
- **Stabile Performance**: Kann große Mengen an Stickern (2000+) verarbeiten
- **WhatsApp Integration**: Nahtlose Integration mit WhatsApp über die offizielle Sticker-API
- **Pack-Verwaltung**: Verwalten, anzeigen und löschen Sie Sticker-Pakete

## Systemanforderungen

- Android 5.0 (API Level 21) oder höher
- WhatsApp installiert
- Speicherberechtigung zum Lesen von Bildern

## Installation

### Aus dem Quellcode bauen

1. **Klonen Sie das Repository**
   ```bash
   git clone https://github.com/FlorianKlz/myfirstrepo.git
   cd myfirstrepo
   ```

2. **Öffnen Sie das Projekt in Android Studio**
   - Öffnen Sie Android Studio
   - Wählen Sie "Open an existing project"
   - Navigieren Sie zum geklonten Repository-Ordner

3. **Bauen Sie die App**
   ```bash
   ./gradlew assembleDebug
   ```

4. **Installieren Sie auf Ihrem Gerät**
   ```bash
   ./gradlew installDebug
   ```

   Oder verwenden Sie Android Studio, um die App direkt auf einem verbundenen Gerät oder Emulator zu installieren.

## Verwendung

### Erste Schritte

1. **Berechtigung erteilen**
   - Beim ersten Start wird die App nach Speicherberechtigung fragen
   - Erteilen Sie die Berechtigung, um auf WebP-Dateien zugreifen zu können

2. **Sticker scannen**
   - Tippen Sie auf "Scan for Stickers"
   - Wählen Sie den Ordner, in dem Ihre WebP-Sticker gespeichert sind:
     - **Pictures folder**: Standard-Bilderordner
     - **Downloads folder**: Download-Ordner
     - **Custom folder**: Scannt das gesamte Gerät
   - Die App scannt nach WebP-Dateien und erstellt automatisch Sticker-Pakete

3. **Sticker zu WhatsApp hinzufügen**
   - Nach dem Scannen werden alle erstellten Sticker-Pakete angezeigt
   - Tippen Sie auf "Add to WhatsApp" bei einem Paket
   - WhatsApp öffnet sich und zeigt das Sticker-Paket
   - Tippen Sie in WhatsApp auf "Hinzufügen", um das Paket zu installieren

4. **Sticker verwenden**
   - Öffnen Sie einen WhatsApp-Chat
   - Tippen Sie auf das Sticker-Symbol
   - Ihre importierten Sticker sind nun verfügbar!

### Pack-Verwaltung

- **Details anzeigen**: Tippen Sie auf ein Sticker-Paket, um alle Sticker anzuzeigen
- **Pack löschen**: Tippen Sie auf "Delete", um ein einzelnes Paket zu löschen
- **Alle löschen**: Tippen Sie auf "Clear All", um alle Pakete zu löschen

## Technische Details

### WhatsApp Sticker-Anforderungen

Die App befolgt alle WhatsApp-Sticker-Richtlinien:
- Mindestens 3 Sticker pro Paket
- Maximal 30 Sticker pro Paket
- Maximale Dateigröße: 100 KB pro Sticker
- Format: WebP mit transparentem Hintergrund
- Tray-Icon: 96x96 Pixel

### Architektur

Die App besteht aus folgenden Hauptkomponenten:

1. **StickerContentProvider**: Content Provider für die WhatsApp-Integration
2. **StickerPackManager**: Verwaltet Sticker-Pakete und Datei-Scanning
3. **MainActivity**: Hauptbildschirm mit Pack-Liste
4. **StickerPackDetailsActivity**: Detailansicht für einzelne Pakete
5. **WhatsAppHelper**: Hilfsfunktionen für WhatsApp-Integration

### Dateistruktur

```
sticker_packs/
├── pack_0_xxxxx/
│   ├── tray_icon.webp
│   ├── sticker_0.webp
│   ├── sticker_1.webp
│   └── ...
├── pack_1_xxxxx/
│   └── ...
└── ...
```

Alle Sticker-Dateien werden im internen App-Speicher unter `sticker_packs/` gespeichert.

## Fehlerbehebung

### "Keine WebP-Dateien gefunden"
- Stellen Sie sicher, dass Ihre Sticker-Dateien die Endung `.webp` haben
- Überprüfen Sie, ob die Dateien im ausgewählten Ordner gespeichert sind
- Stellen Sie sicher, dass die Speicherberechtigung erteilt wurde

### "WhatsApp ist nicht installiert"
- Installieren Sie WhatsApp aus dem Play Store
- Starten Sie die App neu

### Sticker werden nicht in WhatsApp angezeigt
- Stellen Sie sicher, dass Sie das Paket in WhatsApp hinzugefügt haben
- Aktualisieren Sie WhatsApp auf die neueste Version
- Starten Sie WhatsApp neu

### Zu große Dateien
- Die App komprimiert automatisch Dateien über 100 KB
- Wenn die Komprimierung fehlschlägt, wird der Sticker übersprungen
- Verwenden Sie ein externes Tool, um Dateien vor dem Import zu komprimieren

## Bekannte Einschränkungen

- Android 11+ erfordert möglicherweise zusätzliche Berechtigungen für den Zugriff auf alle Dateien
- Die App kann nur WebP-Dateien verarbeiten (keine PNG, JPG, etc.)
- Sehr große Sammlungen (>5000 Dateien) können längere Scan-Zeiten haben

## Entwicklung

### Voraussetzungen

- Android Studio Arctic Fox oder neuer
- JDK 8 oder höher
- Android SDK 34
- Gradle 8.1+

### Projekt-Setup

```bash
# Abhängigkeiten installieren
./gradlew build

# Tests ausführen
./gradlew test

# APK erstellen
./gradlew assembleRelease
```

### Beiträge

Beiträge sind willkommen! Bitte erstellen Sie einen Pull Request mit:
- Klarer Beschreibung der Änderungen
- Tests für neue Features
- Aktualisierte Dokumentation

## Lizenz

Dieses Projekt steht unter der MIT-Lizenz.

## Danksagungen

- WhatsApp für die Sticker-API-Dokumentation
- Die Android Open Source Community

## Kontakt

Bei Fragen oder Problemen erstellen Sie bitte ein Issue auf GitHub.

---

**Hinweis**: Diese App ist nicht offiziell mit WhatsApp verbunden oder von WhatsApp unterstützt.
