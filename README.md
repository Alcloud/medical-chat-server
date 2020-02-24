# Messaging-Server

Eine angepasste Signal-Server (1.88) Version. Die Version enthält keinen Contact Discovery Service. 

# Notwendige Software

* Java 1.8
* Redis (für Caching-Zwecke)
* PostgreSQL instance
* Amazon AWS S3 bucket Dienst für Anhänge von Nachrichten
* Firebase Cloud Messaging (FCM) Account vom Google Developer Console. (A senderId add a ApiKey. Create a project, senderId is the projectNumber. For ApiKey, go to API management -> credentials -> create credentials -> API Keys -> Server Key and save this key)
* APN account for Apple Push Notifications (two pem certificates) - wenn Apple-Geräte verwendet werden

# Installationsschritte (Docker)

## Building des Servers

1. Clone dies Projekt, dann gib ein:
2. cd signal-server
3. mvn clean install -DskipTests
4. TextSecureServer-1.88.jar muss in das "package/files" kopiert werden.

(Wir überspringen die Tests, da sie fehlschlagen, wenn wir keinen Schlüssel finden, den wir später behandeln werden).
Für das Server-Deployment wird ein Compose Tool (zum Definieren und Ausführen von mehreren Containern) verwendet. 
Ein docker-compose.yml Datei Beispiel ist [hier](https://gitlab.fokus.fraunhofer.de/movi/signal-server/blob/master/docker-compose.yml) zu finden.

Insgesamt sollen 3 Container gestarten werden: Redis, PostgreSQL und Server. Zuerst müssen entsprechende Docker Images gebuildet werden.

## PostgreSQL Image

1. Clone das [Repository](https://gitlab.fokus.fraunhofer.de/movi/signal-deployment), dann gib ein:
2. cd Postgres
3. docker build -t postgres .

## Server Image

Vor dem Server Image Building soll die Config-Datei ["sample.yml"](https://gitlab.fokus.fraunhofer.de/movi/signal-server/blob/master/config/sample1.yml) angepasst werden. 
Siehe "Creating Config file" in dieser Doku.

1. Die Datei sample.yml muss in das "package/files" kopiert werden.
2. cd signal-server/package
3. docker build -t signal_server .

## Datenbanken erstellen

Für die korrekte Kompilierung müssen zuerst 2 Postgres Datenbanken erstellt werden:
In der Konsole:

1. docker create --name postgres postgres
2. docker start postgres
3. docker exec -it postgres psql -U postgres -c "create database accountdb"
4. docker exec -it postgres psql -U postgres -c "create database messagedb"
5. docker rm --force postgres

Der Container muss gelöscht werden, da ein neuer wird im docker-compose erstellt.

# Launch the server

1. docker network create movi-signal
2. cd signal-server
2. docker-compose up

Der Server wird auf http://localhost:9002 erreichbar.

# Creating Config file

Vor dem Start des Servers muss die Konfigurationsdatei konfiguriert werden. Im Config Ordner gibt es die Konfigurationsdatei ["sample.yml"](https://gitlab.fokus.fraunhofer.de/movi/signal-server/blob/master/config/sample1.yml), die kann als ein Muster verwendet werden.
Folgende Felder müssen angepasst werden:

## AWS S3 configuration
Die Schlüsseln kann man in der [AWS Console](https://console.aws.amazon.com/console/home) finden:

IAM -> Benutzer -> Sicherheitsanmeldeinformationen -> Zugriffsschlüssel Erstellen:  

wo <b>accessKey</b> ist Zugriffsschlüssel-ID und
<b>accessSecret</b> ist Geheimer Zugriffsschlüssel. 

* attachments: 
  - accessKey: AKIA...VV3NA
  - accessSecret: Uawi9ykwV8scO...g3ezDfUvr0tApF
  - bucket: musterName


* profiles:
  - accessKey: AKIA...VV3NA
  - accessSecret: Uawi9ykwV8scO...g3ezDfUvr0tApF
  - bucket: musterName
  - region: eu-west-1

## Postgresql database configuration
Die PostgreSQL- und Docker-Konfiguration kann man [hier](https://gitlab.fokus.fraunhofer.de/movi/signal-deployment/tree/master/Postgres) finden.

* database: 
  - driverClass: org.postgresql.Driver
  - user: postgres
  - password: test123test123
  - url: jdbc:postgresql://postgres:5432/accountdb

## Postgresql database configuration for message store
* messageStore: 
  - driverClass: org.postgresql.Driver
  - user: postgres
  - password: test123test123
  - url: jdbc:postgresql://postgres:5432/messagedb

## FCM Configuration
Die Daten kann man in der [Firebase Console](https://console.firebase.google.com/?pli=1) finden.
Projekteinstellungen -> Cloud Messaging:

wo <b>senderId</b> ist eine Projektnummer und  
<b>apiKey</b> ist "Alter Serverschlüssel".

* gcm: 
  - senderId: 9470...4136
  - apiKey: AIzaSyCH0VC5...86el7KniqriCvIQ