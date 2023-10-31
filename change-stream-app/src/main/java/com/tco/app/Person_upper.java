package com.tco.app;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Field;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(exclude = {"id", "dateOfBirth"})
public class Person_upper {

  @Id private String id;

  // annotated as class attributes (camel case) will be mapped 
  // onto db fields that have a different name (upper case)
  @Field("FIRST_NAME")
  private String firstName;
  @Field("SECOND_NAME")
  private String secondName;
  @Field("DATE_OF_BIRTH")
  private LocalDateTime dateOfBirth;
  @Field("PROFESSION")
  private String profession;
  @Field("SALARY")
  private int salary;


  public Person_upper(
      final String id,
      final String firstName,
      final String secondName,
      final LocalDateTime dateOfBirth,
      final String profession,
      final int salary) {
    this.id = id;
    this.firstName = firstName;
    this.secondName = secondName;
    this.dateOfBirth = dateOfBirth;
    this.profession = profession;
    this.salary = salary;
  }

    public Person_upper(
      final String firstName,
      final String secondName,
      final LocalDateTime dateOfBirth,
      final String profession,
      final int salary) {
    this.firstName = firstName;
    this.secondName = secondName;
    this.dateOfBirth = dateOfBirth;
    this.profession = profession;
    this.salary = salary;
  }

  // Lombok adds the getters, setters and toString
}

