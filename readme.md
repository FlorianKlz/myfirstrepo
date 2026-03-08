# Sticker Importer für WhatsApp

Diese App bereitet beliebig viele WEBP-Sticker von deinem Gerätespeicher in 30er-Paketen auf und macht sie direkt als WhatsApp-Sticker verfügbar. Sie kopiert die Dateien lokal in die App, legt automatisch Stickerpakete an und bietet pro Paket eine Import-Schaltfläche für WhatsApp und WhatsApp Business.

## Installation
1. Projekt in Android Studio (Giraffe oder neuer) öffnen.
2. Beim ersten Build werden die fehlenden Android- und Compose-Abhängigkeiten automatisch geladen.
3. Eine Debug- oder Release-APK bauen und auf dem Gerät installieren.

## Nutzung
1. App starten und die Speicherberechtigung erlauben.
2. Den Ordner mit deinen WEBP-Stickern über „Ordner auswählen“ wählen (z. B. ein Download- oder Backup-Ordner).
3. „Stickerpakete vorbereiten“ tippen. Die App gruppiert automatisch bis zu 30 Sticker pro Paket und legt sie lokal ab.
4. Für jedes erzeugte Paket „Zu WhatsApp hinzufügen“ tippen. WhatsApp öffnet den Import-Dialog; Vorgang bestätigen.

## Hinweise und Grenzen
- Unterstützt WEBP-Dateien; andere Formate werden ignoriert.
- Pakete werden der WhatsApp-Vorgabe entsprechend in Blöcken von maximal 30 Stickern erzeugt.
- Die Dateien werden in den App-Speicher kopiert, sodass sie verfügbar bleiben, auch wenn der Ursprungsordner geändert wird.
- Sticker, die den WhatsApp-Richtlinien nicht entsprechen (z. B. zu groß oder falsche Abmessungen), müssen manuell angepasst werden.
