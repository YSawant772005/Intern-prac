package com.phonebook.controller;

import com.phonebook.dto.ContactPageResponse;
import com.phonebook.dto.ContactResponse;
import com.phonebook.exception.GlobalExceptionHandler;
import com.phonebook.exception.InvalidContactException;
import com.phonebook.service.ContactService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ContactController.class)
@Import(GlobalExceptionHandler.class)
class ContactControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ContactService contactService;

    @Test
    void returnsPaginatedContacts() throws Exception {
        ContactResponse contact = ContactResponse.from(contact(1, "Jane Doe", "9876543210"));
        when(contactService.getContacts(0, 20, null, null))
                .thenReturn(new ContactPageResponse(List.of(contact), 0, 20, 1, 1, true, true));

        mockMvc.perform(get("/contacts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Jane Doe"))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.last").value(true));
    }

    @Test
    void rejectsInvalidPageSize() throws Exception {
        mockMvc.perform(get("/contacts?page=-1&size=20"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Page must be zero or greater."));
    }

    @Test
    void passesSearchSortAndPaginationToService() throws Exception {
        ContactResponse contact = ContactResponse.from(contact(1, "Rahul Sharma", "9876500001"));
        when(contactService.getContacts(2, 20, "rahul", "name,asc"))
                .thenReturn(new ContactPageResponse(List.of(contact), 2, 20, 41, 3, false, false));

        mockMvc.perform(get("/contacts")
                        .param("search", "rahul")
                        .param("page", "2")
                        .param("size", "20")
                        .param("sort", "name,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Rahul Sharma"))
                .andExpect(jsonPath("$.page").value(2))
                .andExpect(jsonPath("$.totalElements").value(41))
                .andExpect(jsonPath("$.totalPages").value(3));

        verify(contactService).getContacts(2, 20, "rahul", "name,asc");
    }

    @Test
    void passesDescendingNameSort() throws Exception {
        when(contactService.getContacts(0, 20, null, "name,desc"))
                .thenReturn(new ContactPageResponse(List.of(), 0, 20, 0, 0, true, true));

        mockMvc.perform(get("/contacts").param("sort", "name,desc"))
                .andExpect(status().isOk());

        verify(contactService).getContacts(0, 20, null, "name,desc");
    }

    @Test
    void rejectsInvalidSortDirection() throws Exception {
        when(contactService.getContacts(0, 20, null, "name,diagonal"))
                .thenThrow(new InvalidContactException("Sort direction must be asc or desc."));

        mockMvc.perform(get("/contacts").param("sort", "name,diagonal"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Sort direction must be asc or desc."));
    }

    @Test
    void rejectsMalformedJson() throws Exception {
        mockMvc.perform(post("/contacts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{not-json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Request body must contain valid JSON."));
    }

    @Test
    void rejectsInvalidContact() throws Exception {
        mockMvc.perform(post("/contacts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Jane\"}"))
                .andExpect(status().isBadRequest());
    }

    private static com.phonebook.model.Contact contact(int id, String name, String phone) {
        com.phonebook.model.Contact contact = new com.phonebook.model.Contact();
        contact.setId(id);
        contact.setName(name);
        contact.setPhoneNumber(phone);
        contact.setCreatedAt(Instant.parse("2026-01-01T00:00:00Z"));
        return contact;
    }
}
