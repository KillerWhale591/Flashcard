package com.zeyufu.flashcard;

public class User {
    public String FirstName;
    public String Email;
    public String LastName;
    public String userId;
    public User() {

    }

    public User(String userId,String email,String firstName, String LastName) {
        this.userId = userId;
        this.Email = email;
        this.FirstName = firstName;
        this.LastName = LastName;
    }
}
