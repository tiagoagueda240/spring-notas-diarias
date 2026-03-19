package com.tiagoagueda.api.core.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuração global do Spring MVC.
 * A anotação @Configuration indica ao Spring que esta classe contém configurações que
 * devem ser carregadas no arranque da aplicação.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * Configura o CORS (Cross-Origin Resource Sharing).
     * O CORS é um mecanismo de segurança dos browsers que impede que um site (ex: localhost:4200)
     * faça pedidos a uma API noutro domínio/porto (ex: localhost:8080) sem permissão.
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // Aplica-se a todos os endpoints da nossa API
                .allowedOrigins("http://localhost:4200") // Permite pedidos do frontend em Angular
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // Métodos HTTP permitidos
                .allowedHeaders("*"); // Permite o envio de qualquer cabeçalho (ex: Authorization)
    }
}