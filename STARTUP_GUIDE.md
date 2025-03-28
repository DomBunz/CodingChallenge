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

## Services starten

```bash
# Starten der Backend-Services
mvn spring-boot:run
```
Das Frontend ist unter http://localhost:8080/login erreichbar.

## API-Dokumentation

Die API ist mit Swagger/OpenAPI dokumentiert. Nach dem Start der Anwendung können Sie die API-Dokumentation unter folgenden URLs aufrufen:

- Swagger UI: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- OpenAPI JSON: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

Die API ist in drei Gruppen unterteilt:

1. **Premium Calculation API** - Endpunkte für die Prämienberechnung und zugehörige Daten
   - URL: [http://localhost:8080/v3/api-docs/premium-calculation](http://localhost:8080/v3/api-docs/premium-calculation)
   
2. **Application Management API** - Endpunkte für die Verwaltung von Versicherungsanträgen
   - URL: [http://localhost:8080/v3/api-docs/application-management](http://localhost:8080/v3/api-docs/application-management)

3. **Factor Management API** - Endpunkte für die Verwaltung von Faktoren (nur für Administratoren)
   - URL: [http://localhost:8080/v3/api-docs/factor-management](http://localhost:8080/v3/api-docs/factor-management)

### Authentifizierung

Die meisten API-Endpunkte erfordern eine Authentifizierung. Die Anwendung verwendet HTTP Basic Authentication. Folgende Benutzer sind standardmäßig verfügbar:

| Benutzername | Passwort   | Rolle(n)   |
|--------------|------------|------------|
| admin        | admin      | ADMIN      |
| agent        | agent      | AGENT      |
| customer     | customer   | CUSTOMER   |
| api_client   | api_client | API_CLIENT |

Die Authentifizierung erfolgt über den HTTP-Header `Authorization` mit dem Wert `Basic <base64-encoded-credentials>`. Dabei ist `<base64-encoded-credentials>` die Base64-Kodierung von `username:password`.

Beispiel für den Benutzer `admin` mit Passwort `admin`:
```
Authorization: Basic YWRtaW46YWRtaW4=
```

### REST API

Die Anwendung bietet (unter anderem) folgende REST-Endpunkte:

#### Premium Calculation API

| Methode | Endpunkt | Beschreibung |
|---------|----------|--------------|
| POST | `/api/premium/calculate` | Berechnet die Versicherungsprämie basierend auf Postleitzahl, Fahrzeugtyp und jährlicher Kilometerleistung |
| GET | `/api/premium/factors` | Liefert alle verfügbaren Faktoren (Region, Fahrzeug, Kilometerleistung) |
| GET | `/api/premium/factors/region` | Liefert alle verfügbaren Region-Faktoren |
| GET | `/api/premium/factors/vehicle` | Liefert alle verfügbaren Fahrzeugtyp-Faktoren |
| GET | `/api/premium/factors/mileage` | Liefert alle verfügbaren Kilometerleistungs-Faktoren |
| GET | `/api/premium/postcodes` | Liefert alle Postleitzahlen mit zugehörigen Bundesländern und Städten (unterstützt Paginierung) |
| GET | `/api/premium/postcodes/search/{prefix}` | Sucht nach Postleitzahlen, die mit einem bestimmten Präfix beginnen |

#### Application API

| Methode | Endpunkt | Beschreibung |
|---------|----------|--------------|
| POST | `/api/applications` | Erstellt eine neue Versicherungsanfrage |
| GET | `/api/applications` | Gibt alle Versicherungsanfragen zurück (unterstützt Paginierung) |
| GET | `/api/applications/{id}` | Gibt eine spezifische Versicherungsanfrage zurück |
| GET | `/api/applications/status/{status}` | Gibt alle Versicherungsanfragen mit einem bestimmten Status zurück (unterstützt Paginierung) |
| PUT | `/api/applications/{id}/status/{status}` | Aktualisiert den Status einer Versicherungsanfrage |
| DELETE | `/api/applications/{id}` | Löscht eine Versicherungsanfrage |

### Paginierung

Die Endpunkte, die Paginierung unterstützen, akzeptieren die folgenden Parameter:

- `page`: Die Seitennummer (0-basiert)
- `size`: Die Anzahl der Elemente pro Seite (Standard: 20)
- `sort`: Sortierparameter im Format `property,direction` (z.B. `createdAt,desc`)

Beispiele:

1. Erste Seite mit 10 Anträgen, sortiert nach Erstellungsdatum absteigend:
```
GET /api/applications?page=1&size=10&sort=createdAt,desc
```

2. Zweite Seite von Anträgen mit Status NEW:
```
GET /api/applications/status/NEW?page=2&size=15
```

3. Sortierung nach berechneter Prämie aufsteigend:
```
GET /api/applications?sort=calculatedPremium,asc
```

Die Antwort enthält nicht nur die angeforderten Daten, sondern auch Paginierungsmetadaten wie:
- Gesamtzahl der Elemente
- Gesamtzahl der Seiten
- Aktuelle Seitennummer
- Angabe, ob es weitere Seiten gibt

ACHTUNG: Gegebenenfalls muss `&` in der URL mittels `\` escaped werden.

## Beispiel-Anfragen

### Prämienberechnung

**cURL:**
```bash
curl -X POST http://localhost:8080/api/premium/calculate \
  -H "Content-Type: application/json" \
  -H "Authorization: Basic YWRtaW46YWRtaW4=" \
  -d '{
    "postalCode": "10115",
    "vehicleType": "Mittelklasse",
    "annualMileage": 15000
  }'
```

**PowerShell (Invoke-WebRequest):**
```powershell
$body = @{
    postalCode = "10115"
    vehicleType = "Mittelklasse"
    annualMileage = 15000
} | ConvertTo-Json

$headers = @{
    "Authorization" = "Basic YWRtaW46YWRtaW4="
}

Invoke-WebRequest -Uri "http://localhost:8080/api/premium/calculate" -Method POST -Body $body -ContentType "application/json" -Headers $headers
```

### Faktoren abrufen

**cURL:**
```bash
curl -X GET http://localhost:8080/api/premium/factors \
  -H "Authorization: Basic YWRtaW46YWRtaW4="
```

**PowerShell (Invoke-WebRequest):**
```powershell
$headers = @{
    "Authorization" = "Basic YWRtaW46YWRtaW4="
}

Invoke-WebRequest -Uri "http://localhost:8080/api/premium/factors" -Method GET -Headers $headers
```

### Fahrzeugtyp-Faktoren abrufen

**cURL:**
```bash
curl -X GET http://localhost:8080/api/premium/factors/vehicle \
  -H "Authorization: Basic YWRtaW46YWRtaW4="
```

**PowerShell (Invoke-WebRequest):**
```powershell
$headers = @{
    "Authorization" = "Basic YWRtaW46YWRtaW4="
}

Invoke-WebRequest -Uri "http://localhost:8080/api/premium/factors/vehicle" -Method GET -Headers $headers
```

### Postleitzahlen abrufen

**cURL:**
```bash
# Alle Postleitzahlen (paginiert, Seite 1 mit 20 Einträgen)
curl -X GET http://localhost:8080/api/premium/postcodes?page=1&size=20 \
  -H "Authorization: Basic YWRtaW46YWRtaW4="

# Alle Postleitzahlen (paginiert, Seite 2 mit 10 Einträgen, sortiert nach Postleitzahl)
curl -X GET http://localhost:8080/api/premium/postcodes?page=2&size=10&sort=postalCode,asc \
  -H "Authorization: Basic YWRtaW46YWRtaW4="
```

**PowerShell (Invoke-WebRequest):**
```powershell
$headers = @{
    "Authorization" = "Basic YWRtaW46YWRtaW4="
}

# Alle Postleitzahlen (paginiert, Seite 1 mit 20 Einträgen)
Invoke-WebRequest -Uri "http://localhost:8080/api/premium/postcodes?page=1&size=20" -Method GET -Headers $headers

# Alle Postleitzahlen (paginiert, Seite 2 mit 10 Einträgen, sortiert nach Postleitzahl)
Invoke-WebRequest -Uri "http://localhost:8080/api/premium/postcodes?page=2&size=10&sort=postalCode,asc" -Method GET -Headers $headers
```

### Postleitzahlen nach Präfix suchen

**cURL:**
```bash
curl -X GET http://localhost:8080/api/premium/postcodes/search/803 \
  -H "Authorization: Basic YWRtaW46YWRtaW4="
```

**PowerShell (Invoke-WebRequest):**
```powershell
$headers = @{
    "Authorization" = "Basic YWRtaW46YWRtaW4="
}

Invoke-WebRequest -Uri "http://localhost:8080/api/premium/postcodes/search/803" -Method GET -Headers $headers
```

### Versicherungsanfrage erstellen

**cURL:**
```bash
curl -X POST http://localhost:8080/api/applications \
  -H "Content-Type: application/json" \
  -H "Authorization: Basic YWRtaW46YWRtaW4=" \
  -d '{"postalCode": "10115", "vehicleType": "Mittelklasse", "annualMileage": 15000}'
```

**PowerShell (Invoke-WebRequest):**
```powershell
$body = @{
    postalCode = "10115"
    vehicleType = "Mittelklasse"
    annualMileage = 15000
} | ConvertTo-Json

$headers = @{
    "Authorization" = "Basic YWRtaW46YWRtaW4="
}

Invoke-WebRequest -Uri "http://localhost:8080/api/applications" `
  -Method POST `
  -ContentType "application/json" `
  -Body $body `
  -Headers $headers
```

### Abrufen aller Versicherungsanfragen

**cURL:**
```bash
curl -X GET http://localhost:8080/api/applications \
  -H "Authorization: Basic YWRtaW46YWRtaW4="
```

**PowerShell (Invoke-WebRequest):**
```powershell
$headers = @{
    "Authorization" = "Basic YWRtaW46YWRtaW4="
}

Invoke-WebRequest -Uri "http://localhost:8080/api/applications" -Method GET -Headers $headers
```

### Abrufen einer spezifischen Versicherungsanfrage

**cURL:**
```bash
curl -X GET http://localhost:8080/api/applications/1 \
  -H "Authorization: Basic YWRtaW46YWRtaW4="
```

**PowerShell (Invoke-WebRequest):**
```powershell
$headers = @{
    "Authorization" = "Basic YWRtaW46YWRtaW4="
}

Invoke-WebRequest -Uri "http://localhost:8080/api/applications/1" -Method GET -Headers $headers
```

### Abrufen von Versicherungsanfragen nach Status

**cURL:**
```bash
curl -X GET http://localhost:8080/api/applications/status/NEW \
  -H "Authorization: Basic YWRtaW46YWRtaW4="
```

**PowerShell (Invoke-WebRequest):**
```powershell
$headers = @{
    "Authorization" = "Basic YWRtaW46YWRtaW4="
}

Invoke-WebRequest -Uri "http://localhost:8080/api/applications/status/NEW" -Method GET -Headers $headers
```

### Aktualisieren des Status einer Versicherungsanfrage

**cURL:**
```bash
curl -X PUT http://localhost:8080/api/applications/1/status/ACCEPTED \
  -H "Authorization: Basic YWRtaW46YWRtaW4="
```

**PowerShell (Invoke-WebRequest):**
```powershell
$headers = @{
    "Authorization" = "Basic YWRtaW46YWRtaW4="
}

Invoke-WebRequest -Uri "http://localhost:8080/api/applications/1/status/ACCEPTED" -Method PUT -Headers $headers
```

### Löschen einer Versicherungsanfrage

**cURL:**
```bash
curl -X DELETE http://localhost:8080/api/applications/1 \
  -H "Authorization: Basic YWRtaW46YWRtaW4="
```

**PowerShell (Invoke-WebRequest):**
```powershell
$headers = @{
    "Authorization" = "Basic YWRtaW46YWRtaW4="
}

Invoke-WebRequest -Uri "http://localhost:8080/api/applications/1" -Method DELETE -Headers $headers
