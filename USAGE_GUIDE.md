# WhatsApp Sticker Importer - Benutzerhandbuch

## Schnellstart-Anleitung

### Schritt 1: App installieren
1. Laden Sie die APK-Datei herunter oder bauen Sie die App aus dem Quellcode
2. Installieren Sie die App auf Ihrem Android-Gerät
3. Stellen Sie sicher, dass WhatsApp installiert ist

### Schritt 2: Berechtigungen erteilen
1. Öffnen Sie die App
2. Wenn Sie nach Berechtigungen gefragt werden, tippen Sie auf "Zulassen"
3. Für Android 11+: Aktivieren Sie "Zugriff auf alle Dateien" in den Einstellungen

### Schritt 3: Sticker vorbereiten
Ihre Sticker sollten:
- Im WebP-Format vorliegen
- Einen transparenten Hintergrund haben (empfohlen)
- Kleiner als 100 KB sein
- In einem Ordner auf Ihrem Gerät gespeichert sein

### Schritt 4: Sticker scannen
1. Tippen Sie auf "Scan for Stickers"
2. Wählen Sie den Speicherort Ihrer Sticker:
   - **Pictures folder**: Für Sticker im Bilder-Ordner
   - **Downloads folder**: Für Sticker im Download-Ordner
   - **Custom folder**: Scannt das gesamte Gerät (kann länger dauern)
3. Warten Sie, bis der Scan abgeschlossen ist
4. Die App erstellt automatisch Pakete mit je 30 Stickern

### Schritt 5: Zu WhatsApp hinzufügen
1. In der Paketliste sehen Sie alle erstellten Sticker-Pakete
2. Tippen Sie bei einem Paket auf "Add to WhatsApp"
3. WhatsApp öffnet sich und zeigt eine Vorschau des Pakets
4. Tippen Sie in WhatsApp auf "HINZUFÜGEN"
5. Das Paket ist jetzt in WhatsApp verfügbar!

### Schritt 6: Sticker verwenden
1. Öffnen Sie einen WhatsApp-Chat
2. Tippen Sie auf das Sticker-Symbol (Smiley mit gefaltetem Eck)
3. Ihre Sticker-Pakete erscheinen unten
4. Tippen Sie auf ein Paket und wählen Sie einen Sticker zum Senden

## Tipps für große Sticker-Sammlungen

### 2000 Sticker importieren
Wenn Sie ~2000 Sticker haben, wird die App:
1. Alle WebP-Dateien finden
2. Sie in ca. 67 Pakete à 30 Sticker aufteilen
3. Jedes Paket einzeln bereitstellen

**Wichtig**: Sie müssen jedes Paket einzeln zu WhatsApp hinzufügen, da WhatsApp keine Massen-Imports unterstützt.

### Empfohlene Vorgehensweise
1. Scannen Sie zunächst nur einen Ordner mit wenigen Stickern zum Testen
2. Überprüfen Sie, ob die Sticker in WhatsApp korrekt angezeigt werden
3. Scannen Sie dann Ihre gesamte Sammlung
4. Fügen Sie die Pakete nach Bedarf zu WhatsApp hinzu

### Performance-Optimierung
- **Ordner organisieren**: Organisieren Sie Sticker in Unterordnern (z.B. nach Thema)
- **Batch-Import**: Importieren Sie nicht alle 2000 Sticker auf einmal, sondern in Gruppen
- **Speicherplatz**: Stellen Sie sicher, dass genug Speicherplatz verfügbar ist (~200-300 MB für 2000 Sticker)

## Häufig gestellte Fragen (FAQ)

### Kann ich auch PNG oder JPG verwenden?
Nein, WhatsApp unterstützt nur WebP-Dateien für Sticker. Sie müssen Ihre Bilder zunächst in WebP konvertieren.

**Konvertierungs-Tools:**
- Online: https://cloudconvert.com/png-to-webp
- Windows: XnConvert
- Mac: ImageMagick
- Linux: `cwebp` Kommandozeilen-Tool

### Wie erstelle ich WebP-Dateien mit transparentem Hintergrund?
1. Öffnen Sie Ihr Bild in einem Bildbearbeitungsprogramm (z.B. GIMP, Photoshop)
2. Entfernen Sie den Hintergrund
3. Exportieren Sie als WebP mit Transparenz

### Warum werden manche Sticker übersprungen?
Sticker werden übersprungen wenn:
- Die Datei größer als 100 KB ist und nicht komprimiert werden kann
- Die Datei beschädigt ist
- Das Format nicht korrekt ist

### Kann ich Pakete nachträglich bearbeiten?
Nein, einmal erstellte Pakete können nicht bearbeitet werden. Sie können aber:
- Das Paket löschen und neu scannen
- Ein neues Paket mit anderen Stickern erstellen

### Wie lösche ich Sticker aus WhatsApp?
1. Öffnen Sie WhatsApp
2. Gehen Sie zu Einstellungen → Chats → Sticker
3. Wischen Sie nach links auf dem Paket und tippen Sie "Löschen"

### Die App findet meine Sticker nicht
**Überprüfen Sie:**
- Dateien haben die Endung `.webp` (nicht `.WEBP` oder `.Webp`)
- Berechtigungen sind erteilt
- Dateien sind auf dem internen Speicher oder SD-Karte (nicht in der Cloud)
- Der gewählte Ordner ist korrekt

### Kann ich eigene Pack-Namen vergeben?
In der aktuellen Version werden automatisch Namen wie "Sticker Pack 1", "Sticker Pack 2" vergeben. Eine zukünftige Version könnte benutzerdefinierte Namen ermöglichen.

## Bekannte Probleme und Lösungen

### Problem: "Berechtigung verweigert"
**Lösung:**
1. Öffnen Sie Android-Einstellungen
2. Apps → WhatsApp Sticker Importer → Berechtigungen
3. Aktivieren Sie "Speicher" oder "Dateien und Medien"

### Problem: App stürzt beim Scannen ab
**Lösung:**
- Zu viele Dateien auf einmal? Versuchen Sie einen kleineren Ordner
- Neustart des Geräts
- App-Cache leeren (Einstellungen → Apps → Sticker Importer → Speicher → Cache leeren)

### Problem: Sticker sehen in WhatsApp pixelig aus
**Ursache:** Die Original-Datei ist zu klein oder wurde zu stark komprimiert
**Lösung:** Verwenden Sie höher aufgelöste Quelldateien (mindestens 512x512 Pixel empfohlen)

### Problem: WhatsApp zeigt "Sticker-Paket nicht gefunden"
**Lösung:**
1. Löschen Sie das Paket in der App
2. Scannen Sie erneut
3. Fügen Sie es wieder zu WhatsApp hinzu
4. Falls das nicht hilft: App neu installieren

## Technische Informationen

### Unterstützte Android-Versionen
- Minimum: Android 5.0 (Lollipop, API 21)
- Empfohlen: Android 11+ (API 30+)

### Speicheranforderungen
- App: ~10 MB
- Sticker-Daten: ~100-150 KB pro Sticker
- 2000 Sticker: ~200-300 MB

### Datenstandort
Alle Sticker werden im internen App-Speicher unter `/data/data/com.whatsapp.stickerimporter/files/sticker_packs/` gespeichert.

## Support und Feedback

Bei Problemen oder Verbesserungsvorschlägen:
1. Erstellen Sie ein Issue auf GitHub
2. Beschreiben Sie das Problem detailliert
3. Fügen Sie Informationen zu Ihrer Android-Version hinzu
4. Screenshots sind hilfreich!

---

Viel Spaß mit Ihren Stickern! 🎉
