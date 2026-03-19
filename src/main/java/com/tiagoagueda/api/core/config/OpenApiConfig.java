package com.tiagoagueda.api.core.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "API Diário Inteligente",
                version = "1.0",
                description = "API para gestão de diários com extração de tarefas via IA (Gemini).",
                contact = @Contact(name = "Nome")
        ),
        security = {
                @SecurityRequirement(name = "bearerAuth") // Diz que todos os endpoints precisam disto por defeito
        }
)
@SecurityScheme(
        name = "bearerAuth",
        description = "Faz login no endpoint /auth/authenticate, copia o token e cola-o aqui.",
        scheme = "bearer",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        in = SecuritySchemeIn.HEADER
)
/**
 * Configuração central do OpenAPI/Swagger.
 *
 * Define metadados da API (nome, versão, descrição) e o esquema de segurança JWT
 * para que os endpoints protegidos possam ser testados no Swagger UI.
 */
public class OpenApiConfig {
}