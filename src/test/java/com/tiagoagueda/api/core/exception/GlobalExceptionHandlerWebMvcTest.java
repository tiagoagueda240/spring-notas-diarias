package com.tiagoagueda.api.core.exception;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.NoSuchElementException;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = GlobalExceptionHandlerWebMvcTest.TestController.class)
@Import(GlobalExceptionHandler.class)
@AutoConfigureMockMvc(addFilters = false)
class GlobalExceptionHandlerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void whenBadCredentials_thenReturns401() throws Exception {
        mockMvc.perform(get("/test-errors/bad-credentials"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("Email ou password incorretos."));
    }

    @Test
    void whenNoSuchElement_thenReturns404() throws Exception {
        mockMvc.perform(get("/test-errors/not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("O registo solicitado não foi encontrado na base de dados."));
    }

    @Test
    void whenUserAlreadyExists_thenReturns409() throws Exception {
        mockMvc.perform(get("/test-errors/user-exists"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Este email já se encontra registado no nosso sistema."));
    }

    @Test
    void whenValidationFails_thenReturns400WithFieldMessage() throws Exception {
        String invalidPayload = """
                {
                  "name": ""
                }
                """;

        mockMvc.perform(post("/test-errors/validation")
                        .contentType("application/json")
                        .content(invalidPayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message", containsString("name")));
    }

    @Test
    void whenUnhandledException_thenReturns500() throws Exception {
        mockMvc.perform(get("/test-errors/unexpected"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value("Ocorreu um erro interno. A nossa equipa já foi notificada."));
    }

    @RestController
    @RequestMapping("/test-errors")
    static class TestController {

        @GetMapping("/bad-credentials")
        ResponseEntity<Void> badCredentials() {
            throw new BadCredentialsException("Credenciais inválidas");
        }

        @GetMapping("/not-found")
        ResponseEntity<Void> notFound() {
            throw new NoSuchElementException("não encontrado");
        }

        @GetMapping("/user-exists")
        ResponseEntity<Void> userExists() {
            throw new UserAlreadyExistsException("Este email já se encontra registado no nosso sistema.");
        }

        @PostMapping("/validation")
        ResponseEntity<Void> validation(@Valid @RequestBody ValidationRequest request) {
            return ResponseEntity.ok().build();
        }

        @GetMapping("/unexpected")
        ResponseEntity<Void> unexpected() {
            throw new RuntimeException("erro inesperado");
        }
    }

    record ValidationRequest(@NotBlank String name) {
    }
}
