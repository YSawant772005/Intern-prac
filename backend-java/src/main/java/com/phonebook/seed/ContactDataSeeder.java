package com.phonebook.seed;

import com.phonebook.model.Contact;
import com.phonebook.repository.ContactRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Set;

@Component
public class ContactDataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(ContactDataSeeder.class);

    private static final int SEED_TARGET = 1000;
    private static final long RANDOM_SEED = 4207L;
    private static final String PHONE_PREFIX_DIGITS = "6789";

    private static final List<String> FIRST_NAMES = List.of(
            "Aarav", "Aditi", "Ajay", "Akshay", "Alok", "Amit", "Ananya", "Anil", "Anjali", "Ankit",
            "Anusha", "Arjun", "Ashok", "Ayesha", "Deepak", "Deepika", "Divya", "Ganesh", "Gaurav", "Harsha",
            "Kavya", "Kiran", "Lakshmi", "Madhav", "Meena", "Meera", "Mohan", "Nandini", "Neha", "Nikhil",
            "Pooja", "Priya", "Rahul", "Rajesh", "Ravi", "Rohit", "Rohan", "Sana", "Sanjay", "Sarita",
            "Shreya", "Suresh", "Sunil", "Swati", "Vikram", "Vinod", "Vishal");

    private static final List<String> LAST_NAMES = List.of(
            "Agarwal", "Ahmed", "Bansal", "Bhat", "Bhattacharya", "Choudhary", "Das", "Desai", "Dube",
            "Goyal", "Gupta", "Iyer", "Jain", "Joshi", "Kapoor", "Khan", "Kulkarni", "Kumar", "Malhotra",
            "Mehta", "Mishra", "Naidu", "Nair", "Pandey", "Patel", "Pillai", "Prasad", "Rao", "Reddy",
            "Roy", "Sharma", "Singh", "Sinha", "Srivastava", "Thakur", "Varma", "Verma");

    private static final List<String> AREAS = List.of(
            "MG Road", "Indiranagar", "Bandra", "Andheri East", "Jayanagar", "Sadar Bazaar",
            "Koregaon Park", "Aundh", "Viman Nagar", "Sector 17", "Connaught Place", "Salt Lake",
            "Banjara Hills", "Gachibowli", "Anna Nagar", "Velachery", "HSR Layout", "Whitefield");

    private static final List<String> CITIES = List.of(
            "Mumbai", "Delhi", "Bengaluru", "Hyderabad", "Chennai", "Kolkata", "Pune", "Ahmedabad",
            "Jaipur", "Lucknow", "Nagpur", "Indore", "Surat", "Coimbatore", "Kochi", "Bhopal",
            "Patna", "Chandigarh");

    private final ContactRepository contactRepository;

    public ContactDataSeeder(ContactRepository contactRepository) {
        this.contactRepository = contactRepository;
    }

    @Override
    public void run(String... args) {
        long existingCount = contactRepository.count();
        if (existingCount >= SEED_TARGET) {
            log.info("Seed skipped: database already contains {} contacts.", existingCount);
            return;
        }

        List<Contact> existing = contactRepository.findAll();
        Set<String> usedPhoneNumbers = new HashSet<>();
        Set<String> usedEmails = new HashSet<>();
        for (Contact contact : existing) {
            if (contact.getPhoneNumber() != null) {
                usedPhoneNumbers.add(contact.getPhoneNumber());
            }
            if (contact.getEmail() != null) {
                usedEmails.add(contact.getEmail());
            }
        }

        int toCreate = (int) (SEED_TARGET - existingCount);
        Random random = new Random(RANDOM_SEED);
        List<Contact> contacts = new ArrayList<>(toCreate);
        for (int i = 0; i < toCreate; i++) {
            String firstName = FIRST_NAMES.get(random.nextInt(FIRST_NAMES.size()));
            String lastName = LAST_NAMES.get(random.nextInt(LAST_NAMES.size()));

            Contact contact = new Contact();
            contact.setName(firstName + " " + lastName);
            contact.setPhoneNumber(nextUniquePhoneNumber(random, usedPhoneNumbers));
            contact.setEmail(nextUniqueEmail(firstName, lastName, i, usedEmails));
            contact.setAddress(AREAS.get(random.nextInt(AREAS.size())) + ", " + CITIES.get(random.nextInt(CITIES.size())));
            contacts.add(contact);
        }

        contactRepository.saveAll(contacts);
        log.info("Seeded {} fake contacts. Total contacts now {}.", contacts.size(), contactRepository.count());
    }

    private static String nextUniquePhoneNumber(Random random, Set<String> usedPhoneNumbers) {
        String phoneNumber;
        StringBuilder builder = new StringBuilder();
        do {
            builder.setLength(0);
            builder.append(PHONE_PREFIX_DIGITS.charAt(random.nextInt(PHONE_PREFIX_DIGITS.length())));
            for (int i = 0; i < 9; i++) {
                builder.append(random.nextInt(10));
            }
            phoneNumber = builder.toString();
        } while (!usedPhoneNumbers.add(phoneNumber));
        return phoneNumber;
    }

    private static String nextUniqueEmail(String firstName, String lastName, int index, Set<String> usedEmails) {
        String base = (firstName + "." + lastName).toLowerCase(Locale.ROOT).replaceAll("[^a-z]", "");
        String email = base + index + "@example.com";
        while (!usedEmails.add(email)) {
            index++;
            email = base + index + "@example.com";
        }
        return email;
    }
}