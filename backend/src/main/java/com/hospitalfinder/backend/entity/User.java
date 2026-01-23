package com.hospitalfinder.backend.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.Getter;
import lombok.Setter;

@Document(collection = "users")
public class User {
    @Id
    @Getter @Setter
    private String id;

    @Getter @Setter
    private String name;
    @Getter @Setter
    private String email;
    @Getter @Setter
    private String phone;
    @Getter @Setter
    private String password;

    @Getter @Setter
    private Integer age;
    @Getter @Setter
    private String gender;

    @Getter @Setter
    private Role role;
}
