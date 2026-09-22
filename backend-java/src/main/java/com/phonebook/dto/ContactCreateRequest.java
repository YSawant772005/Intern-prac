package com.phonebook.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ContactCreateRequest {

    @NotBlank(message = "Name is required.")
    @Size(max = 255, message = "Name cannot exceed 255 characters.")
    private String name;

    @NotBlank(message = "Phone number is required.")
    @JsonProperty("phone_number")
    private String phoneNumber;

    @Email(message = "Invalid email address.")
    @Size(max = 255, message = "Email cannot exceed 255 characters.")
    private String email;

    @Size(max = 10000, message = "Address cannot exceed 10000 characters.")
    private String address;

    public String getName() { return name; }

    public void setName(String name) { this.name = trimToNull(name); }

    @JsonProperty("phone_number")
    public String getPhoneNumber() { return phoneNumber; }

    @JsonProperty("phone_number")
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = trimToNull(phoneNumber); }

    public String getEmail() { return email; }

    public void setEmail(String email) { this.email = trimToNull(email); }

    public String getAddress() { return address; }

    public void setAddress(String address) { this.address = trimToNull(address); }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}