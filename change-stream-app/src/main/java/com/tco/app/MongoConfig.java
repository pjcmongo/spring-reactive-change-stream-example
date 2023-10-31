package com.tco.app;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractReactiveMongoConfiguration;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;


import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;

@Configuration
@EnableReactiveMongoRepositories(basePackageClasses = PersonRepository.class)
public class MongoConfig extends AbstractReactiveMongoConfiguration {

    @Value("${spring.data.mongodb.uri}")
    private String uri;

    @Override
    @Bean
    public MongoClient reactiveMongoClient() {
        return MongoClients.create(uri);
    }

    @Override
    protected String getDatabaseName() {
      return "test";
    }

    @Bean
    public ReactiveMongoTemplate reactiveMongoTemplate() {
      return new ReactiveMongoTemplate(reactiveMongoClient(), getDatabaseName());
    }
}