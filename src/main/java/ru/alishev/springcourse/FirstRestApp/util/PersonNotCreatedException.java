package ru.alishev.springcourse.FirstRestApp.util;

public class PersonNotCreatedException extends RuntimeException {
    public PersonNotCreatedException(String message) {
        super(message);
    }
}
