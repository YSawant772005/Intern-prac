package com.phonebook.service;

import com.phonebook.dto.ContactCreateRequest;
import com.phonebook.dto.ContactPageResponse;
import com.phonebook.exception.DuplicateContactException;
import com.phonebook.exception.InvalidContactException;
import com.phonebook.model.Contact;
import com.phonebook.repository.ContactRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ContactServiceTest {

    @Mock
    private ContactRepository contactRepository;

    @InjectMocks
    private ContactService contactService;

    @Test
    void searchesInDatabaseAndMapsPageMetadata() {
        Contact contact = contact(1, "Jane Doe", "9876543210");
        when(contactRepository.findByNameContainingIgnoreCaseOrPhoneNumberContainingIgnoreCaseOrEmailContainingIgnoreCaseOrAddressContainingIgnoreCase(
            eq("jane"), eq("jane"), eq("jane"), eq("jane"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(contact), Pageable.ofSize(20), 21));

        ContactPageResponse result = contactService.getContacts(0, 20, " jane ", null);

        assertEquals(1, result.content().size());
        assertEquals(21, result.totalElements());
        assertEquals(2, result.totalPages());
        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
        verify(contactRepository).findByNameContainingIgnoreCaseOrPhoneNumberContainingIgnoreCaseOrEmailContainingIgnoreCaseOrAddressContainingIgnoreCase(
            eq("jane"), eq("jane"), eq("jane"), eq("jane"), pageable.capture());
        assertEquals(0, pageable.getValue().getPageNumber());
        assertEquals(20, pageable.getValue().getPageSize());
        assertEquals("createdAt: DESC,id: DESC", pageable.getValue().getSort().toString());
    }

    @Test
    void excludesCurrentContactWhenCheckingDuplicatePhoneOnUpdate() {
        Contact existing = contact(7, "Existing", "9876543210");
        when(contactRepository.findById(7)).thenReturn(Optional.of(existing));
        when(contactRepository.existsByPhoneNumberAndIdNot("9876543210", 7)).thenReturn(false);
        when(contactRepository.saveAndFlush(existing)).thenReturn(existing);

        ContactCreateRequest request = request("Renamed", "9876543210", null);
        contactService.updateContact(7, request);

        verify(contactRepository).existsByPhoneNumberAndIdNot("9876543210", 7);
        verify(contactRepository, never()).existsByPhoneNumber("9876543210");
    }

    @Test
    void rejectsDuplicateEmailBeforeSaving() {
        ContactCreateRequest request = request("Jane", "9876543210", "jane@example.com");
        when(contactRepository.existsByPhoneNumber("9876543210")).thenReturn(false);
        when(contactRepository.existsByEmail("jane@example.com")).thenReturn(true);

        assertThrows(DuplicateContactException.class, () -> contactService.createContact(request));
        verify(contactRepository, never()).saveAndFlush(any(Contact.class));
    }

    @Test
    void sortsByNameAscending() {
        Page<Contact> page = emptyPage();
        when(contactRepository.findAll(any(Pageable.class))).thenReturn(page);

        contactService.getContacts(0, 20, null, "name,asc");

        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
        verify(contactRepository).findAll(pageable.capture());
        assertEquals(0, pageable.getValue().getPageNumber());
        assertEquals(20, pageable.getValue().getPageSize());
        assertEquals("name: ASC,id: DESC", pageable.getValue().getSort().toString());
    }

    @Test
    void sortsByNameDescending() {
        Page<Contact> page = emptyPage();
        when(contactRepository.findAll(any(Pageable.class))).thenReturn(page);

        contactService.getContacts(0, 20, null, "name,desc");

        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
        verify(contactRepository).findAll(pageable.capture());
        assertEquals("name: DESC,id: DESC", pageable.getValue().getSort().toString());
    }

    @Test
    void combinesSearchSortAndPagination() {
        Contact contact = contact(1, "Rahul Sharma", "9876500001");
        Page<Contact> page = new PageImpl<>(List.of(contact),
                PageRequest.of(2, 20, Sort.by(Sort.Order.asc("name"), Sort.Order.desc("id"))), 41);
        when(contactRepository.findByNameContainingIgnoreCaseOrPhoneNumberContainingIgnoreCaseOrEmailContainingIgnoreCaseOrAddressContainingIgnoreCase(
                eq("rahul"), eq("rahul"), eq("rahul"), eq("rahul"), any(Pageable.class)))
                .thenReturn(page);

        ContactPageResponse result = contactService.getContacts(2, 20, " rahul ", "name,asc");

        assertEquals(1, result.content().size());
        assertEquals(41, result.totalElements());
        assertEquals(3, result.totalPages());
        assertEquals(2, result.page());
        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
        verify(contactRepository).findByNameContainingIgnoreCaseOrPhoneNumberContainingIgnoreCaseOrEmailContainingIgnoreCaseOrAddressContainingIgnoreCase(
                eq("rahul"), eq("rahul"), eq("rahul"), eq("rahul"), pageable.capture());
        assertEquals(2, pageable.getValue().getPageNumber());
        assertEquals(20, pageable.getValue().getPageSize());
        assertEquals("name: ASC,id: DESC", pageable.getValue().getSort().toString());
    }

    @Test
    void rejectsNegativePage() {
        assertThrows(InvalidContactException.class, () -> contactService.getContacts(-1, 20, null, null));
    }

    @Test
    void rejectsOversizedPageSize() {
        assertThrows(InvalidContactException.class, () -> contactService.getContacts(0, 101, null, null));
    }

    @Test
    void rejectsUnsupportedSortField() {
        assertThrows(InvalidContactException.class, () -> contactService.getContacts(0, 20, null, "address,asc"));
    }

    @Test
    void rejectsInvalidSortDirection() {
        assertThrows(InvalidContactException.class, () -> contactService.getContacts(0, 20, null, "name,diagonal"));
    }

    private static ContactCreateRequest request(String name, String phone, String email) {
        ContactCreateRequest request = new ContactCreateRequest();
        request.setName(name);
        request.setPhoneNumber(phone);
        request.setEmail(email);
        return request;
    }

    private static Page<Contact> emptyPage() {
        return new PageImpl<>(List.of(),
                PageRequest.of(0, 20, Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id"))), 0);
    }

    private static Contact contact(int id, String name, String phone) {
        Contact contact = new Contact();
        contact.setId(id);
        contact.setName(name);
        contact.setPhoneNumber(phone);
        contact.setCreatedAt(Instant.parse("2026-01-01T00:00:00Z"));
        return contact;
    }
}
