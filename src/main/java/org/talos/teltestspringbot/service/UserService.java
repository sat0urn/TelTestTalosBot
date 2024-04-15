package org.talos.teltestspringbot.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.talos.teltestspringbot.model.User;

@Service
public class UserService {

    private final MongoTemplate mongoTemplate;

    @Autowired
    public UserService(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public void check(String firstName, String lastName, int userId, String userName) {
        User user = mongoTemplate.findOne(Query.query(Criteria.where("userId").is(userId)), User.class);

        if (user == null) {
            user = new User();
            user.setUserId(userId);
            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setUserName(userName);
            mongoTemplate.save(user);
            System.out.println("User not exists in database. Written.");
        } else {
            System.out.println("User exists in database.");
        }
    }
}
