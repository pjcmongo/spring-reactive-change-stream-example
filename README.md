# spring-reactive-change-stream-example

Setup:
- set spring.data.mongodb.uri in spring-reactive-change-stream-example/change-stream-app/src/main/resourcesapplication.properties to MongoDB cluster connection string 
- e.g. mongodb+srv://<username>:<password>@<cluster>/?retryWrites=true&w=majority

Build:
- mvn package -Dmaven.test.skip=true

Run: 
java -jar target/App-1.0-SNAPSHOT.jar com.tco.app.App