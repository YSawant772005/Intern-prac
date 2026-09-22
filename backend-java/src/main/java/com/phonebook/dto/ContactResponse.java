package com.phonebook.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.phonebook.model.Contact;

import java.time.Instant;

public class ContactResponse {

    private final String name;

    @JsonProperty("phone_number")
    private final String phoneNumber;

    private final String email;
    private final String address;
    private final Integer id;

    @JsonProperty("created_at")
    private final Instant createdAt;

    private ContactResponse(Contact contact) {
        this.name = contact.getName();
        this.phoneNumber = contact.getPhoneNumber();
        this.email = contact.getEmail();
        this.address = contact.getAddress();
        this.id = contact.getId();
        this.createdAt = contact.getCreatedAt();
    }

    public static ContactResponse from(Contact contact) { return new ContactResponse(contact); }

    public String getName() { return name; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getEmail() { return email; }
    public String getAddress() { return address; }
    public Integer getId() { return id; }
    public Instant getCreatedAt() { return createdAt; }
}