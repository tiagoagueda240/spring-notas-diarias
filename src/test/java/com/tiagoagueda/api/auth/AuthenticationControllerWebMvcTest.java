package com.tiagoagueda.api.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tiagoagueda.api.auth.dto.AuthenticationResponse;
import com.tiagoagueda.api.auth.dto.RegisterRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthenticationController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthenticationControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthenticationService authenticationService;

    @Test
    void register_WhenPayloadIsValid_Returns200AndToken() throws Exception {
        RegisterRequest request = new RegisterRequest("Nome", "nome@email.com", "123456");
        when(authenticationService.register(any(RegisterRequest.class)))
                .thenReturn(new AuthenticationResponse("jwt-token-123"));

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token-123"));

        verify(authenticationService).register(any(RegisterRequest.class));
    }

    @Test
    void register_WhenPayloadIsInvalid_Returns400AndDoesNotCallService() throws Exception {
        String invalidRequestJson = """
                {
                  "name": "",
                  "email": "email-invalido",
                  "password": "123"
                }
                """;

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequestJson))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(authenticationService);
    }
}
