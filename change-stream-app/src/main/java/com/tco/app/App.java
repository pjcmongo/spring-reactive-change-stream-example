package com.tco.app;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

import org.bson.BsonDocument;
import org.bson.BsonValue;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.*;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.mongodb.core.ChangeStreamEvent;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;


import com.mongodb.client.model.changestream.ChangeStreamDocument;
import com.mongodb.client.model.changestream.OperationType;
import com.mongodb.client.model.changestream.UpdateDescription;


import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.aggregation.SetOperation;
import org.springframework.data.mongodb.core.aggregation.Aggregation;

import reactor.core.publisher.Flux;

import org.springframework.boot.*;

@SpringBootApplication public class App implements CommandLineRunner {
    @Autowired private PersonRepository personRepository;
    @Autowired ReactiveMongoTemplate template;

    private static ConfigurableApplicationContext ctx;

    public static void main(String args[]) {
        ctx = SpringApplication.run(App.class);
    }


    @Override public void run(String... args) {

        HashMap<String,String> fieldMap = new HashMap<String,String>();
        fieldMap.put("SALARY","salary");
        fieldMap.put("FIRST_NAME","firstName");
        fieldMap.put("SECOND_NAME","secondName");
        fieldMap.put("PROFESSION","profession");


        template.dropCollection("person_upper").subscribe();
        template.dropCollection("person").subscribe();
        try {
            Thread.sleep(2000);
        }
        catch(Exception e) {

        }
        System.out.println("*** create change stream on source collection");

        // change stream filter 
        Criteria opTypes = new Criteria().orOperator(
            Criteria.where("operationType").is("insert"),
            Criteria.where("operationType").is("update"),
            Criteria.where("operationType").is("delete")
        );
        MatchOperation match = new MatchOperation(opTypes);

        // change stream aggregation to $set fields for destination collection
        SetOperation set = null; 
        Iterator<Entry<String,String>> fit = fieldMap.entrySet().iterator();
        while(fit.hasNext())
        {
            Entry<String, String> e = fit.next();
            if(set == null) {
                set = SetOperation.set("fullDocument."+e.getValue()).toValue("$fullDocument."+e.getKey());
            }
            else {
                set = set.and().set("fullDocument."+e.getValue()).toValue("$fullDocument."+e.getKey());
            }
        }
        Aggregation agg = Aggregation.newAggregation(match,set);
        System.out.println("change stream aggregation: " + agg);

        // set change stream on source collection (containing Person_upper)
        // map to destination collection (containing Person)
        Flux<ChangeStreamEvent<Person>> flux = template.changeStream(Person.class) 
            .watchCollection("person_upper") 
            .filter(agg)                                           
            .listen();  

        flux.doOnNext(event -> {
            System.out.println("Change Stream Event Received: " + event);
            if (event.getOperationType() == OperationType.UPDATE) {
                ChangeStreamDocument<Document> doc = event.getRaw();
                UpdateDescription desc = doc.getUpdateDescription();
                BsonDocument updates = desc.getUpdatedFields();
                Set<Entry<String,BsonValue>> s = updates.entrySet();
                Iterator<Entry<String,BsonValue>> it = s.iterator();
                Update update = new Update();
                while(it.hasNext()) {
                    Entry<String,BsonValue> e = it.next();
                    System.out.println("update: " + e.getKey() + " " + e.getValue());
                    update.set(fieldMap.get(e.getKey()),e.getValue());
                }
                System.out.println("update op: " + update);
                Query IdQuery = new Query(Criteria.where("_id").is(doc.getFullDocument().get("_id")));
                System.out.println("query op: " + IdQuery);
                template.updateFirst(IdQuery, update, Person.class,"person").subscribe(System.out::println);
            }
            else if(event.getOperationType() == OperationType.INSERT) {
                Person inserted = event.getBody();
                System.out.println("insert doc: " + inserted);
                template.insert(inserted,"person").subscribe();
            }
            else if(event.getOperationType() == OperationType.DELETE) {
                
                ChangeStreamDocument<Document> doc = event.getRaw();
                Object id = doc.getDocumentKey().get("_id");
                Query IdQuery = new Query(Criteria.where("_id").is(id));
                System.out.println("delete doc: "+ id);
                template.remove(IdQuery,Person.class).subscribe(System.out::println);
            }
        })
	    .subscribe();

        // delay for change stream registration
        try {
            Thread.sleep(2000);
        }
        catch(Exception e) {

        }

        System.out.println("** insert into source collection via repository");

        final Person_upper johnAoe = new Person_upper("john", "aoe", LocalDateTime.now(), "loser", 0);
        final Person_upper johnBoe = new Person_upper("john", "boe", LocalDateTime.now(), "a bit of a loser", 10);
        final Person_upper johnCoe = new Person_upper("john", "coe", LocalDateTime.now(), "average", 100);
        final Person_upper johnDoe = new Person_upper("john", "doe", LocalDateTime.now(), "winner", 1000);
        personRepository.saveAll(Flux.just(johnAoe, johnBoe, johnCoe, johnDoe)).subscribe();
        
        // delay for insert / change stream events 
        try {
            Thread.sleep(2000);
        }
        catch(Exception e) {
        }

        System.out.println("*** perform find in destination collection");
        Query query = new Query();
        List<Criteria> criterias = new ArrayList<>();
        Criteria and1 = new Criteria().andOperator(
            Criteria.where("salary").is(10),
            Criteria.where("firstName").is("john")
        );
        criterias.add(and1);
        Criteria and2 = new Criteria().andOperator(
            Criteria.where("salary").is(0),
            Criteria.where("firstName").is("john")
        );
        criterias.add(and2);
        Criteria all = new Criteria().orOperator(criterias.toArray(new Criteria[criterias.size()]));
        query.addCriteria(all);
        template.find(query, Person.class, "person").subscribe(System.out::println);

        try {
            Thread.sleep(1000);
        }
        catch(Exception e) {
        }

        System.out.println("\n****");
        System.out.println("**** Updates/deletes in person_upper collection will be replicated to person collection");
    }
}
