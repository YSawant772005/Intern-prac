package com.phonebook.dto;

import java.util.List;

public record ContactPageResponse(
        List<ContactResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last) {
}