# Versicherungsprämien-Service: Startup Guide

Diese Anleitung beschreibt, wie Sie das Versicherungsprämien-Berechnungssystem lokal einrichten und ausführen können.

## Voraussetzungen

- Java 21 JDK
- Maven
- Docker und Docker Compose

## Datenbank starten

Die Anwendung verwendet PostgreSQL als Datenbank, die über Docker bereitgestellt wird.

```bash
# Starten der PostgreSQL-Datenbank
docker-compose up -d postgres
```

Die Datenbank ist dann unter folgenden Parametern erreichbar:
- **Host**: localhost
- **Port**: 5432
- **Datenbank**: insurance_premium
- **Benutzer**: insurance_user
- **Passwort**: insurance_password

Um die Datenbank zu stoppen:

```bash
docker-compose down
```

Um die Datenbank zu stoppen und alle Daten zu löschen:

```bash
docker-compose down -v
```

## Backend-Services starten

TODO

## Frontend starten

TODO

## API-Dokumentation

TODO

## Beispiel-Anfragen

TODO
