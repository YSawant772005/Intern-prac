package com.phonebook.exception;

public class ContactNotFoundException extends RuntimeException {

    public ContactNotFoundException() {
        super("Contact not found.");
    }
}