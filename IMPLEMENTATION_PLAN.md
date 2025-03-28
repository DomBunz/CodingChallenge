# Implementierungsplan: Versicherungsprämien-Berechnungsservice

## Übersicht des Projekts

Dieses Dokument beschreibt den Implementierungsplan für einen Service zur Berechnung von Versicherungsprämien basierend auf Kilometerleistung, Fahrzeugtyp und Region. Der Service soll sowohl eine Benutzeroberfläche für Antragsteller als auch eine HTTP-API für Drittanbieter bereitstellen.

## Architektur

### Modularer Monolith

Nach sorgfältiger Abwägung habe ich mich für eine **modulare Monolith-Architektur** entschieden. Diese Entscheidung basiert auf der Einfachheit, Testbarkeit und Wartbarkeit, die für dieses Projekt besonders wichtig sind. Die Anwendung wird in klar definierte Module unterteilt, die jeweils eine spezifische Domäne abdecken:

1. **Prämienkalkulations-Modul**
   - Kernfunktionalität zur Berechnung der Versicherungsprämie
   - Implementierung der Berechnungslogik (Kilometerleistung-Faktor, Fahrzeugtyp-Faktor, Region-Faktor)
   - Zugriff auf die Region-Daten aus der CSV-Datei

2. **Antrags-Modul**
   - Verwaltung der Anträge und Nutzereingaben
   - Persistierung der Antragsdaten und berechneten Prämien
   - Nutzung des Prämienkalkulations-Moduls für Berechnungen

3. **API-Modul**
   - Einheitlicher Zugangspunkt für Benutzeroberfläche und Drittanbieter
   - Bereitstellung von REST-Endpunkten
   - Authentifizierung und Autorisierung (falls erforderlich)

Diese Module werden als separate Packages innerhalb derselben Anwendung implementiert, was die Entwicklung, das Testen und die Wartung vereinfacht, während gleichzeitig eine klare Trennung der Zuständigkeiten gewährleistet wird.

### Kommunikation zwischen Modulen

Die Kommunikation zwischen den Modulen erfolgt über klar definierte Service-Interfaces. Dies ermöglicht eine lose Kopplung und erleichtert das Testen durch Mocking. Zudem wird dadurch eine mögliche zukünftige Migration zu einer Microservice-Architektur erleichtert, falls die Anwendung skaliert werden muss.

## Technologie-Stack

### Backend

- **Programmiersprache**: Java 21
- **Framework**: Spring Boot 3.x
  - Spring Web für REST-APIs und Web-MVC
  - Spring Data JPA für Datenbankzugriff
  - Thymeleaf für Server-side Rendering
- **Build-Tool**: Maven

### Datenbank

Ich verwende eine **PostgreSQL**-Datenbank aus folgenden Gründen:
- Open-Source und weit verbreitet
- Hohe Zuverlässigkeit und Skalierbarkeit
- Gute Integration mit Spring Data JPA
- Gute Performance für Lese- und Schreiboperationen
- Einfache Installation via Docker für lokales Setup

### Datenbankmigrationen

Für die Verwaltung und Versionierung des Datenbankschemas wird **Flyway** eingesetzt:
- Versionierte SQL-Migrationsskripte
- Automatische Ausführung bei Anwendungsstart
- Konsistente Datenbankschemata über alle Umgebungen hinweg
- Nahtlose Integration mit Spring Boot
- Unterstützung für initiale Datenladung (z.B. Import der CSV-Daten)

Flyway bietet eine robuste, produktionsreife Lösung für Datenbankmigrationen und ist besonders gut für die kontinuierliche Entwicklung und Deployment geeignet.

### Frontend (optional)

- **Template Engine**: Thymeleaf
  - Nahtlose Integration mit Spring Boot
  - Natürliche Templating-Syntax
  - Gute Unterstützung für Formulare und Validierung
- **CSS-Framework**: Bootstrap 5
  - Responsive Design
  - Fertige UI-Komponenten
  - Einfache Integration mit Thymeleaf
- **JavaScript**: Minimaler Einsatz von Vanilla JavaScript
  - Grundlegende Client-seitige Validierung
  - Dynamische UI-Elemente (bei Bedarf)

Diese Technologiewahl bietet einen einfacheren Ansatz als ein komplexes Frontend-Framework wie Angular und ist besser geeignet für ein Projekt dieser Größe und Komplexität.

## Implementierungsdetails

### Datenmodell

#### Entitäten

1. **Regionsfaktor**
   - ID
   - Bundesland
   - Faktor

1. **Region**
   - ID
   - Bundesland
   - Land
   - Stadt/Ort
   - Postleitzahl
   - Bezirk
   - FK Regionsfaktor

2. **Fahrzeugtyp**
   - ID
   - Bezeichnung
   - Fahrzeugtypfaktor

3. **Antrag**
   - ID
   - Kilometerleistung
   - FK Fahrzeugtyp
   - Postleitzahl
   - Basisprämie
   - Verwendeter Faktor
   - Berechnete Prämie
   - Erstellungsdatum
   - Status

4. **System-Config**
   - ID
   - Schlüssel
   - Wert
   - Beschreibung
   - Letzte Änderung

### Datenbankmigrationen

Die Datenbankmigrationen werden mit Flyway in versionierten SQL-Skripten implementiert:

1. **V1__create_schema.sql**
   - Erstellung der Basistabellen (Region, Fahrzeugtyp, Antrag)
   - Definition von Primärschlüsseln und Fremdschlüsselbeziehungen
   - Indizes für optimierte Abfragen

2. **V2__load_initial_data.sql**
   - Import der Regionsdaten aus der CSV-Datei
   - Erstellung der Standard-Fahrzeugtypen und ihrer Faktoren
   - Initialisierung der Kilometerleistungs-Faktoren

Die Migrationsskripte werden im Ressourcenverzeichnis `src/main/resources/db/migration` abgelegt und automatisch bei Anwendungsstart ausgeführt.

### Modul-Implementierung

#### Prämienkalkulations-Modul

1. **RegionService**
   - Import der CSV-Daten in die Datenbank
   - Abfrage der Regionsdaten anhand der Postleitzahl
   - Bereitstellung des Regionsfaktors

2. **FahrzeugtypService**
   - Verwaltung der Fahrzeugtypen und ihrer Faktoren
   - CRUD-Operationen für Fahrzeugtypen

3. **KalkulationsService**
   - Implementierung der Berechnungslogik
   - Bestimmung des Kilometerleistungsfaktors
   - Berechnung der Gesamtprämie

#### Antrags-Modul

1. **AntragService**
   - Erstellung und Verwaltung von Anträgen
   - Persistierung der Antragsdaten
   - Nutzung des KalkulationsService für Prämienberechnungen

2. **AntragRepository**
   - Datenbankzugriff für Anträge
   - Abfragen und Filterung von Anträgen

### Web-Interface und API-Endpunkte

#### Thymeleaf Templates (optional)

1. **Hauptseite (index.html)**
   - Einführung und Übersicht des Services
   - Navigation zu anderen Seiten

2. **Antragsformular (application-form.html)**
   - Formular zur Eingabe der Antragsdaten (Kilometerleistung, Fahrzeugtyp, Postleitzahl)
   - Client-seitige Validierung mit HTML5 und JavaScript
   - Anzeige der berechneten Prämie

3. **Antragsübersicht (applications.html)**
   - Tabellarische Darstellung aller Anträge
   - Filtermöglichkeiten
   - Detailansicht einzelner Anträge

4. **Detailansicht (application-details.html)**
   - Detaillierte Anzeige eines Antrags
   - Berechnungsdetails

#### Controller

1. **WebController (optional)**
   - Mapping von URL-Pfaden zu Thymeleaf-Templates
   - Formularverarbeitung
   - Weiterleitung und Modellerstellung

2. **RestController**
   - Bereitstellung der REST-API für Drittanbieter

#### REST-API

- `GET /api/premium/factors` - Liefert alle verfügbaren Faktoren (Region, Fahrzeugtyp, Kilometerleistung)
- `GET /api/premium/factors/region` - Liefert alle verfügbaren Region-Faktoren
- `GET /api/premium/factors/vehicle` - Liefert alle verfügbaren Fahrzeugtyp-Faktoren
- `GET /api/premium/factors/mileage` - Liefert alle verfügbaren Kilometerleistungs-Faktoren
- `GET /api/premium/postcodes` - Liefert alle Postleitzahlen (mit Paginierung)
- `GET /api/premium/postcodes/search/{prefix}` - Sucht Postleitzahlen, die mit dem angegebenen Präfix beginnen
- `POST /api/premium/calculate` - Berechnet die Prämie basierend auf den Eingabeparametern

- `POST /api/applications` - Erstellt einen neuen Antrag
- `GET /api/applications/{id}` - Ruft einen bestimmten Antrag ab
- `GET /api/applications` - Ruft alle Anträge ab (mit Paginierung)
- `GET /api/applications/status/{status}` - Ruft alle Anträge mit einem bestimmten Status ab (mit Paginierung)
- `PUT /api/applications/{id}/status/{status}` - Aktualisiert den Status eines Antrags
- `DELETE /api/applications/{id}` - Löscht einen Antrag

- `GET /api/admin/configurations` - Liefert alle Systemkonfigurationen
- `GET /api/admin/configurations/{key}` - Liefert eine bestimmte Systemkonfiguration anhand des Schlüssels
- `POST /api/admin/configurations` - Erstellt eine neue Systemkonfiguration
- `PUT /api/admin/configurations/{key}` - Aktualisiert eine bestehende Systemkonfiguration

- `GET /api/admin/premium/management/regions` - Liefert alle Region-Faktoren
- `GET /api/admin/premium/management/regions/{id}` - Liefert einen bestimmten Region-Faktor
- `POST /api/admin/premium/management/regions` - Erstellt einen neuen Region-Faktor
- `PUT /api/admin/premium/management/regions/{id}` - Aktualisiert einen bestehenden Region-Faktor
- `DELETE /api/admin/premium/management/regions/{id}` - Löscht einen Region-Faktor

- `GET /api/admin/premium/management/vehicles` - Liefert alle Fahrzeugtypen
- `GET /api/admin/premium/management/vehicles/{id}` - Liefert einen bestimmten Fahrzeugtyp
- `POST /api/admin/premium/management/vehicles` - Erstellt einen neuen Fahrzeugtyp
- `PUT /api/admin/premium/management/vehicles/{id}` - Aktualisiert einen bestehenden Fahrzeugtyp
- `DELETE /api/admin/premium/management/vehicles/{id}` - Löscht einen Fahrzeugtyp

- `GET /api/admin/premium/management/mileages` - Liefert alle Kilometerleistungs-Faktoren
- `GET /api/admin/premium/management/mileages/{id}` - Liefert einen bestimmten Kilometerleistungs-Faktor
- `POST /api/admin/premium/management/mileages` - Erstellt einen neuen Kilometerleistungs-Faktor
- `PUT /api/admin/premium/management/mileages/{id}` - Aktualisiert einen bestehenden Kilometerleistungs-Faktor
- `DELETE /api/admin/premium/management/mileages/{id}` - Löscht einen Kilometerleistungs-Faktor

## Sicherheitsimplementierung (optional)

Die Sicherheit der Anwendung ist ein kritischer Aspekt, insbesondere wenn es um Versicherungsdaten und Drittanbieter-Integrationen geht. Für dieses Projekt wird Spring Security als Sicherheitsframework implementiert.

### Warum Spring Security?

Spring Security wurde aus folgenden Gründen ausgewählt:

1. **Native Integration**: Spring Security integriert sich nahtlos in das bestehende Spring Boot-Ökosystem der Anwendung.
2. **Umfassend**: Es bietet robuste Lösungen für Authentifizierung, Autorisierung, Kryptographie und Session-Management.
3. **Aktive Entwicklung**: Spring Security wird aktiv weiterentwickelt und mit regelmäßigen Updates versorgt.
4. **Flexibilität**: Es unterstützt verschiedene Authentifizierungsquellen (LDAP, Datenbank, OAuth2/OIDC).
5. **Moderne Sicherheitsfeatures**: Bietet standardmäßig Schutz vor gängigen Sicherheitsbedrohungen wie CSRF, XSS und mehr.

### Sicherheitsarchitektur

#### 1. Authentifizierungs- und Autorisierungsablauf

```
Benutzer/API-Client → Authentication Filter → UserDetailsService (DB) → Authorization Check → Geschützte Ressourcen
```

#### 2. Rollenbasierte Zugriffssteuerung

Basierend auf den Anforderungen der Anwendung werden folgende Rollen und Berechtigungen implementiert:

##### Rollen

1. **ROLE_ADMIN**
   - Systemadministratoren, die die Anwendung verwalten
   - Vollzugriff auf alle Funktionen und administrativen Funktionen

2. **ROLE_AGENT**
   - Versicherungsagenten, die Anträge bearbeiten
   - Können Anträge erstellen und einsehen
   - Können Prämien berechnen

3. **ROLE_CUSTOMER**
   - Endbenutzer, die Versicherungen beantragen
   - Können eigene Anträge erstellen
   - Können den Status ihrer eigenen Anträge und Prämienberechnungen einsehen

4. **ROLE_API_CLIENT**
   - Drittsysteme, die sich mit der API integrieren
   - Beschränkt auf bestimmte API-Endpunkte

##### Berechtigungen

Die Berechtigungsstruktur wird über Spring Security's Method-Level Security mit `@PreAuthorize` Annotationen implementiert:

1. **Antragsverwaltung**
   - `hasAuthority('APPLICATION_CREATE')` - Neue Anträge erstellen
   - `hasAuthority('APPLICATION_READ')` - Antragsdetails anzeigen
   - `hasAuthority('APPLICATION_READ_ALL')` - Alle Anträge anzeigen (für Agenten/Admins)
   - `hasAuthority('APPLICATION_UPDATE')` - Antragsdetails aktualisieren
   - `hasAuthority('APPLICATION_DELETE')` - Anträge löschen

2. **Prämienberechnung**
   - `hasAuthority('PREMIUM_CALCULATE')` - Prämien berechnen
   - `hasAuthority('PREMIUM_FACTORS_READ')` - Prämienfaktoren anzeigen
   - `hasAuthority('PREMIUM_FACTORS_UPDATE')` - Prämienfaktoren aktualisieren (nur Admin)

3. **Benutzerverwaltung**
   - `hasAuthority('USER_CREATE')` - Benutzer erstellen
   - `hasAuthority('USER_READ')` - Benutzerdetails anzeigen
   - `hasAuthority('USER_READ_ALL')` - Alle Benutzer anzeigen (nur Admin)
   - `hasAuthority('USER_UPDATE')` - Benutzerdetails aktualisieren
   - `hasAuthority('USER_DELETE')` - Benutzer löschen

4. **Systemverwaltung**
   - `hasAuthority('SYSTEM_CONFIG_READ')` - Systemkonfiguration anzeigen

### Zusätzliche Sicherheitsaspekte

1. **Passwortspeicherung**: Verwendung der integrierten Hashing-Funktionen von Spring Security mit Salting für sichere Passwortspeicherung.

2. **HTTPS**: Konfiguration der Anwendung für die Verwendung von HTTPS in der Produktion mit entsprechenden Zertifikaten.

3. **CSRF-Schutz**: Implementierung von CSRF-Tokens für Webformulare, falls implementiert.

4. **Audit-Logging**: Protokollierung aller sicherheitsrelevanten Ereignisse für Audit-Zwecke.

5. **Session-Management**: Konfiguration von angemessenen Session-Timeouts und -Management.

6. **Eingabevalidierung**: Validierung aller Benutzereingaben zur Verhinderung von Injection-Angriffen.

## Testkonzept

### Unit Tests

- Tests für die Berechnungslogik
- Tests für die Service-Methoden
- Tests für die Repository-Methoden

### Integrationstests

- Tests für die API-Endpunkte
- Tests für die Datenbankintegration
- Tests für die Modul-Interaktionen

### End-to-End Tests

- Tests für den gesamten Antragsablauf
- Tests für die Benutzeroberfläche

### Testframework

Ich würde folgende Frameworks verwenden:
- **JUnit 5** für Unit-Tests
- **Mockito** für Mocking
- **Spring Boot Test** für Integrationstests
- **Testcontainers** für Datenbanktests mit echter Datenbankinstanz
- **Selenium** für UI-Tests (optional)

## Qualitätssicherung

- **Continuous Integration** mit GitHub Actions oder ähnlichem (out of scope)
- **Code-Qualitätsanalyse** mit SonarLint/SonarQube
- **Code-Reviews** für alle Änderungen (out of scope)
- **Automatisierte Tests** für alle Komponenten
- **Dokumentation** der API mit Swagger/OpenAPI

## Implementierungsreihenfolge

1. **Setup des Projekts (done)**
   - Einrichtung der Projektstruktur (done)
   - Konfiguration der Datenbank (done)
   - Einrichtung von Flyway für Datenbankmigrationen (done)

2. **Kernfunktionalität (done)**
   - Implementierung des Datenmodells (done)
   - Erstellung der Flyway-Migrationsskripte (done)
   - Implementierung der Berechnungslogik (done)
   - Import der CSV-Daten (done)

3. **API-Entwicklung (done)**
   - Implementierung der REST-Endpunkte (done)
   - Dokumentation der API (done)

4. **Tests und Qualitätssicherung (done)**
   - Implementierung der Tests (done, omitted for now: security components, web controllers, full integration tests)
   - Code-Qualitätsanalyse (automatisch durch SonarLint)
   - Performance-Tests (wenn nötig, aktuell kein Bedarf)

5. **Optional**
   - Implementierung von Spring Security (done)
   - Implementierung der Thymeleaf-Templates (done)

## Mögliche zukünftige Erweiterungen

### Migration zu Microservices

Die modulare Struktur des Monolithen wurde so konzipiert, dass sie eine einfache Migration zu einer Microservice-Architektur ermöglicht, falls die Anwendung in Zukunft skaliert werden muss. Die klaren Modulgrenzen und definierten Interfaces würden eine Extraktion der Module in separate Services erleichtern. Eine solche Migration würde beispielsweise das Nutzen von AWS Lambdas ermöglichen.

Potenzielle Microservices könnten sein:
1. **Prämienkalkulations-Service**
2. **Antrags-Service**
3. **API-Gateway**

### Erweiterung der API-Sicherheit mit JWT

Die initiale Implementierung verwendet API-Schlüssel für die Authentifizierung von Drittanbieter-Integrationen. In Zukunft könnte die API-Sicherheit durch die Implementierung von JWT (JSON Web Tokens) erweitert werden:

   - Zustandslose Authentifizierung, die weniger Datenbankabfragen erfordert
   - Erhöhte Sicherheit durch signierte und optional verschlüsselte Tokens
   - Möglichkeit, zusätzliche Ansprüche (Claims) und Metadaten im Token zu speichern
   - Standardisiertes Format, das von vielen Client-Bibliotheken unterstützt wird

### Erweiterung der Benutzeroberfläche

Bei Bedarf könnte die Benutzeroberfläche später durch ein modernes Frontend-Framework wie Angular, React oder Vue.js ersetzt werden, während die bestehende REST-API weiterhin genutzt wird.

## Abschluss

Dieser Implementierungsplan bietet einen umfassenden Überblick über die Entwicklung des Versicherungsprämien-Berechnungsservices als modularer Monolith mit Thymeleaf-basiertem Frontend. Die vorgeschlagene Architektur und Technologien wurden mit Blick auf Einfachheit, Testbarkeit und Wartbarkeit ausgewählt, wie in den Anforderungen spezifiziert, während gleichzeitig die Möglichkeit für zukünftige Skalierung berücksichtigt wurde.
