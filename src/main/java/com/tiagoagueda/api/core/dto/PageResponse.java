package com.tiagoagueda.api.core.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.domain.Page;
import java.util.List;

// Usamos <T> (Generics) para que este DTO possa embrulhar qualquer tipo de lista!
/**
 * DTO genérico de paginação para respostas da API.
 *
 * Este record evita expor diretamente a estrutura interna Page<T> do Spring.
 */
public record PageResponse<T>(
    @Schema(description = "Conteúdo da página")
        List<T> content,

    @Schema(description = "Número da página atual (começa em 0)", example = "0")
        int pageNumber,

    @Schema(description = "Tamanho da página", example = "10")
        int pageSize,

    @Schema(description = "Total de elementos disponíveis", example = "42")
        long totalElements,

    @Schema(description = "Total de páginas", example = "5")
        int totalPages,

    @Schema(description = "Indica se é a última página", example = "false")
        boolean last
) {
    // Método "fábrica" para converter a Page suja do Spring nesta nossa Page limpa
    /**
     * Converte Page<T> (Spring Data) no formato de resposta padrão da API.
     */
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