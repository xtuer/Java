package com.xtuer.repository;

import bean.Person;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface PersonRepository extends MongoRepository<Person, Long> {
    List<Person> findByFirstName(String firstName);
    List<Person> findByLastName(String lastName);
}
