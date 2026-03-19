package com.tiagoagueda.api.core.dto;

import org.springframework.data.domain.Page;
import java.util.List;

// Usamos <T> (Generics) para que este DTO possa embrulhar qualquer tipo de lista!
public record PageResponse<T>(
        List<T> content,
        int pageNumber,
        int pageSize,
        long totalElements,
        int totalPages,
        boolean last
) {
    // Método "fábrica" para converter a Page suja do Spring nesta nossa Page limpa
    public static <T> PageResponse<T> of(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }
}