package com.github.repositories;

public class UserNotFoundException extends RuntimeException {
    UserNotFoundException(String message) {
        super(message);
    }
}
