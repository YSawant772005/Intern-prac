package com.phonebook.service;

import com.phonebook.dto.ContactCreateRequest;
import com.phonebook.dto.ContactPageResponse;
import com.phonebook.dto.ContactResponse;
import com.phonebook.exception.ContactNotFoundException;
import com.phonebook.exception.DuplicateContactException;
import com.phonebook.exception.InvalidContactException;
import com.phonebook.model.Contact;
import com.phonebook.repository.ContactRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
public class ContactService {

    private static final String PHONE_PATTERN = "^\\+?[0-9][0-9\\s().-]*$";

    private final ContactRepository contactRepository;

    public ContactService(ContactRepository contactRepository) {
        this.contactRepository = contactRepository;
    }

    @Transactional(readOnly = true)
    public ContactPageResponse getContacts(int page, int size, String search, String sort) {
        if (page < 0) {
            throw new InvalidContactException("Page must be zero or greater.");
        }
        if (size < 1 || size > 100) {
            throw new InvalidContactException("Size must be between 1 and 100.");
        }

        String normalizedSearch = search == null || search.isBlank() ? null : search.trim();
        if (normalizedSearch != null && normalizedSearch.length() > 100) {
            throw new InvalidContactException("Search must not exceed 100 characters.");
        }

        Pageable pageable = PageRequest.of(page, size, parseSort(sort));
        Page<Contact> contacts = normalizedSearch == null
                ? contactRepository.findAll(pageable)
                : contactRepository.findByNameContainingIgnoreCaseOrPhoneNumberContainingIgnoreCaseOrEmailContainingIgnoreCaseOrAddressContainingIgnoreCase(
                        normalizedSearch, normalizedSearch, normalizedSearch, normalizedSearch, pageable);

        return new ContactPageResponse(
                contacts.getContent().stream().map(ContactResponse::from).toList(),
                contacts.getNumber(),
                contacts.getSize(),
                contacts.getTotalElements(),
                contacts.getTotalPages(),
                contacts.isFirst(),
                contacts.isLast());
    }

    @Transactional(readOnly = true)
    public ContactResponse getContact(Integer id) {
        return ContactResponse.from(findContact(id));
    }

    @Transactional
    public ContactResponse createContact(ContactCreateRequest request) {
        validateBusinessRules(request);
        ensureUnique(request, null);
        Contact contact = new Contact();
        apply(request, contact);
        return ContactResponse.from(save(contact));
    }

    @Transactional
    public ContactResponse updateContact(Integer id, ContactCreateRequest request) {
        validateBusinessRules(request);
        Contact contact = findContact(id);
        ensureUnique(request, id);
        apply(request, contact);
        return ContactResponse.from(save(contact));
    }

    @Transactional
    public void deleteContact(Integer id) {
        contactRepository.delete(findContact(id));
    }

    private Contact findContact(Integer id) {
        return contactRepository.findById(id).orElseThrow(ContactNotFoundException::new);
    }

    private void ensureUnique(ContactCreateRequest request, Integer currentId) {
        boolean duplicatePhone = currentId == null
                ? contactRepository.existsByPhoneNumber(request.getPhoneNumber())
                : contactRepository.existsByPhoneNumberAndIdNot(request.getPhoneNumber(), currentId);
        if (duplicatePhone) {
            throw new DuplicateContactException("Duplicate phone number.");
        }

        if (request.getEmail() != null) {
            boolean duplicateEmail = currentId == null
                    ? contactRepository.existsByEmail(request.getEmail())
                    : contactRepository.existsByEmailAndIdNot(request.getEmail(), currentId);
            if (duplicateEmail) {
                throw new DuplicateContactException("Duplicate email.");
            }
        }
    }

    private void validateBusinessRules(ContactCreateRequest request) {
        if (request.getName() != null && request.getName().chars().anyMatch(Character::isDigit)) {
            throw new InvalidContactException("Name cannot contain numbers.");
        }
        String phone = request.getPhoneNumber();
        if (phone != null && (!phone.matches(PHONE_PATTERN) || phone.replaceAll("\\D", "").length() != 10)) {
            throw new InvalidContactException("Phone number must contain exactly 10 digits.");
        }
    }

    private Sort parseSort(String sort) {
        if (sort == null || sort.isBlank()) {
            return Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id"));
        }

        String[] parts = sort.trim().split(",", -1);
        if (parts.length != 2) {
            throw new InvalidContactException("Sort must use field,direction format.");
        }

        String property = switch (parts[0]) {
            case "name" -> "name";
            case "phoneNumber" -> "phoneNumber";
            case "email" -> "email";
            case "createdAt" -> "createdAt";
            case "id" -> "id";
            default -> throw new InvalidContactException("Unsupported sort field.");
        };
        Sort.Direction direction;
        try {
            direction = Sort.Direction.fromString(parts[1]);
        } catch (IllegalArgumentException exception) {
            throw new InvalidContactException("Sort direction must be asc or desc.");
        }
        return Sort.by(new Sort.Order(direction, property), new Sort.Order(Sort.Direction.DESC, "id"));
    }

    private void apply(ContactCreateRequest request, Contact contact) {
        contact.setName(request.getName());
        contact.setPhoneNumber(request.getPhoneNumber());
        contact.setEmail(request.getEmail());
        contact.setAddress(request.getAddress());
    }

    private Contact save(Contact contact) {
        try {
            return contactRepository.saveAndFlush(contact);
        } catch (DataIntegrityViolationException exception) {
            String detail = exception.getMostSpecificCause().getMessage();
            String normalized = detail == null ? "" : detail.toLowerCase(Locale.ROOT);
            if (normalized.contains("email")) {
                throw new DuplicateContactException("Duplicate email.");
            }
            if (normalized.contains("phone")) {
                throw new DuplicateContactException("Duplicate phone number.");
            }
            throw exception;
        }
    }
}