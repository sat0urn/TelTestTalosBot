package org.talos.teltestspringbot.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "users")
@Getter
@Setter
public class User {
    @Id
    private String id;
    private String firstName;
    private String lastName;
    private int userId;
    private String userName;
}
