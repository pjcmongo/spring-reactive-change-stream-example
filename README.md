# spring-reactive-change-stream-example

Setup:
- set spring.data.mongodb.uri in spring-reactive-change-stream-example/change-stream-app/src/main/resourcesapplication.properties

- build
mvn package -Dmaven.test.skip=true

- run
java -jar target/App-1.0-SNAPSHOT.jar com.tco.app.App