README

Simple Java Servlet by CSL
Using Spring Boot and Maven

application.properties defines port for localhost

To start app:
>mvnw.cmd spring-boot:run


> Setup database
 in windows ODBC aanmaken met user zoals in application.properties
 
 via mysql user aanmaken en rechten geven
 Nuttige commandos:
 > select user from mysql.user; <-- hierin moet de user zitten uit je property file

 > create database test <-- de database naam uit de property file/ODBC SETTING
 
 > GRANT ALL PRIVILEGES ON test.* TO arjan <-- SOWIESO ALTIJD GOED :P. arjan is mn user uit de settigngs
 
 
 Als je SQL Developer wilt gebruiken, dan moet je daarna MySQL toevoegen als plugin:\
 https://stackoverflow.com/questions/89696/how-do-you-connect-to-a-mysql-database-using-oracle-sql-developer\
Hier wordt wel de oude jar gebruikt, maar pak de versie 8 jar ipv 5.1 (Hij zit al in je maven als het goed is)\
C:\Users\arjan\.m2\repository\mysql\mysql-connector-java\8.0.11

Swagger is te bereiken via:\
http://localhost:8090/swagger-ui.html

Interfaces:
Opslaan van voedsel:
POST /food/<Naam voedsel> <-- HIERNAAR TOE REQUEST MET MACRO PER 100
