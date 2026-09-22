package com.phonebook.seed;

import com.phonebook.model.Contact;
import com.phonebook.repository.ContactRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ContactDataSeederTest {

    @Mock
    private ContactRepository contactRepository;

    @Test
    void skipsSeedingWhenDatabaseAlreadyHasEnoughContacts() {
        when(contactRepository.count()).thenReturn(1000L);

        new ContactDataSeeder(contactRepository).run();

        verify(contactRepository, never()).saveAll(any());
    }

    @Test
    void seedsValidUniqueContactsUpToTargetWithoutReusingExistingData() {
        Contact existingPhone = existing(1, "Yash Ramesh Sawant", "9702216202", "tt@example.com");
        Contact existingNoEmail = existing(2, "zd", "0970221620", null);
        when(contactRepository.count()).thenReturn(3L);
        when(contactRepository.findAll()).thenReturn(List.of(existingPhone, existingPhone, existingNoEmail));

        new ContactDataSeeder(contactRepository).run();

        ArgumentCaptor<List<Contact>> captor = ArgumentCaptor.forClass(List.class);
        verify(contactRepository).saveAll(captor.capture());
        List<Contact> seeded = captor.getValue();
        assertEquals(997, seeded.size());

        Set<String> phoneNumbers = new HashSet<>();
        Set<String> emails = new HashSet<>();
        for (Contact contact : seeded) {
            assertNotNull(contact.getName());
            assertFalse(contact.getName().chars().anyMatch(Character::isDigit));
            assertTrue(contact.getPhoneNumber().matches("[6-9]\\d{9}"));
            assertTrue(phoneNumbers.add(contact.getPhoneNumber()));
            assertNotNull(contact.getEmail());
            assertTrue(emails.add(contact.getEmail()));
            assertNotNull(contact.getAddress());
            assertTrue(contact.getAddress().contains(", "));
        }

        assertEquals(997, phoneNumbers.size());
        assertEquals(997, emails.size());
        assertFalse(phoneNumbers.contains("9702216202"));
        assertFalse(phoneNumbers.contains("0970221620"));
        assertFalse(emails.contains("tt@example.com"));
    }

    private static Contact existing(int id, String name, String phone, String email) {
        Contact contact = new Contact();
        contact.setId(id);
        contact.setName(name);
        contact.setPhoneNumber(phone);
        contact.setEmail(email);
        return contact;
    }
}