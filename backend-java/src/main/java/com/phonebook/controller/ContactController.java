package com.phonebook.controller;

import com.phonebook.dto.ContactCreateRequest;
import com.phonebook.dto.ContactResponse;
import com.phonebook.service.ContactService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import com.phonebook.dto.ContactPageResponse;
import org.springframework.web.bind.annotation.RequestParam;

@Validated
@RestController
@RequestMapping("/contacts")
public class ContactController {

    private final ContactService contactService;

    public ContactController(ContactService contactService) {
        this.contactService = contactService;
    }

    @GetMapping
    public ContactPageResponse getContacts(
            @RequestParam(defaultValue = "0") @Min(value = 0, message = "Page must be zero or greater.") int page,
            @RequestParam(defaultValue = "20") @Min(value = 1, message = "Size must be between 1 and 100.") @Max(value = 100, message = "Size must be between 1 and 100.") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String sort) {
        return contactService.getContacts(page, size, search, sort);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ContactResponse createContact(@Valid @RequestBody ContactCreateRequest request) {
        return contactService.createContact(request);
    }

    @GetMapping("/{id}")
    public ContactResponse getContact(@PathVariable @Positive Integer id) {
        return contactService.getContact(id);
    }

    @PutMapping("/{id}")
    public ContactResponse updateContact(
            @PathVariable @Positive Integer id,
            @Valid @RequestBody ContactCreateRequest request) {
        return contactService.updateContact(id, request);
    }

    @DeleteMapping("/{id}")
    public Map<String, String> deleteContact(@PathVariable @Positive Integer id) {
        contactService.deleteContact(id);
        return Map.of("message", "Contact deleted successfully.");
    }
}