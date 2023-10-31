# spring-reactive-change-stream-example

Sample application that based on Spring Data MongoDB Reactive to leverages MongoDB change streams to replicate from a source collection to a destination collection.
The field names in source collection are camel case while field names in destination collection are upper case. 

Setup:
- set spring.data.mongodb.uri in spring-reactive-change-stream-example/change-stream-app/src/main/resources/application.properties to MongoDB cluster connection string 
- e.g. mongodb+srv://<username>:<password>@<cluster>/?retryWrites=true&w=majority

Build:
- mvn package -Dmaven.test.skip=true

Run: 
- java -jar target/App-1.0-SNAPSHOT.jar com.tco.app.App