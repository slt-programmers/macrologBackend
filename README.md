## Macrolog Backend

application.yaml defines port for localhost

## Database local setup
Make sure there's a version of mysql db running on your OS. 
DBeaver is a helpful tool for this (https://dbeaver.io/download/).
The datasource configuration in application.yml contains the user and password for the database.

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


## Starting the app
Commandline maven
- mvn spring-boot:run

If you haven't maven installed
- mvnw.cmd spring-boot:run

If you get a connection exception the first time you start the app, open de database in DBeaver first.
This magically fixes it. As of 31-10-2025 it still does.


## Swagger
http://localhost:8080/swagger-ui/index.html
https://macrolog-backend.herokuapp.com/swagger-ui/index.html
To use swagger you can authenticate yourself with the green 'Authorize' button on the right side.
Authorize yourself by filling in the JWT token, not including the word 'Bearer'.
This should authorize you for every API call all at once.

## Database export
Open cmd as admin
Navigate to the mysql bin folder (C:\Program Files\MySQL\MySQL Server 8.0\bin on windows)
To generate a backup
- mysqldump -u [user name] –p [password] [database name] > [dumpfilename.sql]
To restore a dumped db
- mysqldump -u [user name] -p [password] [database name] < [dumpfilename.sql]


## Heroku
https://dashboard.heroku.com/apps

To connect a git repo to heroku, 
see https://dashboard.heroku.com/apps/macrolog-backend/deploy/heroku-git 


## Automatic deployment
The backend is configured to automatically deploy to heroku. 
This is configured on the heroku dashboard. 
Pushing to develop results in deployment to ACC.
Pushing to main results in deployment to PRD.




