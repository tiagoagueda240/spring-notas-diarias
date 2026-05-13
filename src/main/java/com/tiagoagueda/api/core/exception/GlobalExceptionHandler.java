package com.tiagoagueda.api.core.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;

/**
 * Intercetor Global de Exceções.
 * O @ControllerAdvice atua como um "para-quedas". Se ocorrer um erro em qualquer Controller,
 * o Spring vem aqui procurar como deve transformar esse erro numa resposta HTTP.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    // Criamos um Logger para a classe de erros
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // 1. Password errada no Login (Devolve 401 Unauthorized)
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException ex) {
        log.warn("Tentativa de login falhada: credenciais inválidas.");
        return buildResponse(HttpStatus.UNAUTHORIZED, "Email ou password incorretos.");
    }

    // 2. Registo não encontrado, ex: user não existe ou .orElseThrow() (Devolve 404 Not Found)
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(NoSuchElementException ex) {
        log.warn("Recurso não encontrado: {}", ex.getMessage());
        return buildResponse(HttpStatus.NOT_FOUND, "O registo solicitado não foi encontrado na base de dados.");
    }

    // 3. Erros de Validação dos DTOs do @Valid (Devolve 400 Bad Request bem formatado)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex) {
// Apanha erros lançados pelo @Valid (ex: @NotBlank, @Email) nos DTOs
        String errorMsg = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .findFirst()
                .orElse("Dados de entrada inválidos.");

        log.warn("Erro de validação recebido do cliente: {}", errorMsg);
        return buildResponse(HttpStatus.BAD_REQUEST, errorMsg);
    }

    // 4. Captura erros inesperados de código (Devolve 500 Internal Server Error)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAllExceptions(Exception ex) {
        log.error("Erro inesperado e grave no servidor: ", ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Ocorreu um erro interno. A nossa equipa já foi notificada.");
    }

    // 5. Utilizador já existe (Devolve 409 Conflict)
    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleUserAlreadyExists(UserAlreadyExistsException ex) {
        log.warn("Tentativa de registo falhada: {}", ex.getMessage());
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage());
    }

    // 6. Token inválido/expirado — refresh token, reset de password (Devolve 401 Unauthorized)
    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ErrorResponse> handleInvalidToken(InvalidTokenException ex) {
        log.warn("Token inválido ou expirado: {}", ex.getMessage());
        return buildResponse(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    // 7. JSON inválido / body malformado (Devolve 400 Bad Request)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadableMessage(HttpMessageNotReadableException ex) {
        log.warn("Pedido com body inválido ou JSON malformado: {}", ex.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, "O corpo do pedido está malformado ou contém valores inválidos.");
    }

    // 8. Parâmetro de URL/query com tipo errado, ex: UUID inválido (Devolve 400 Bad Request)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        log.warn("Tipo de argumento inválido no parâmetro '{}': {}", ex.getName(), ex.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST,
                "Valor inválido para o parâmetro '" + ex.getName() + "'.");
    }

    // 9. Validação de datas / argumentos de negócio (Devolve 400 Bad Request)
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("Argumento inválido: {}", ex.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    private ResponseEntity<ErrorResponse> buildResponse(HttpStatus status, String message) {
        ErrorResponse error = new ErrorResponse(status.value(), message, LocalDateTime.now());
        return new ResponseEntity<>(error, status);
    }
}