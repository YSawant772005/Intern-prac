package com.phonebook.repository;

import com.phonebook.model.Contact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ContactRepository extends JpaRepository<Contact, Integer> {

    Page<Contact> findByNameContainingIgnoreCaseOrPhoneNumberContainingIgnoreCaseOrEmailContainingIgnoreCaseOrAddressContainingIgnoreCase(
            String name, String phoneNumber, String email, String address, Pageable pageable);

    boolean existsByPhoneNumberAndIdNot(String phoneNumber, Integer id);

    boolean existsByEmailAndIdNot(String email, Integer id);

    boolean existsByPhoneNumber(String phoneNumber);

    boolean existsByEmail(String email);
}