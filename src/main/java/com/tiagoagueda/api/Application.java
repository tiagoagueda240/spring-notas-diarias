package com.tiagoagueda.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
/**
 * Classe de arranque da aplicação Spring Boot.
 *
 * O método main inicializa o contexto Spring e levanta a API HTTP.
 */
public class Application {

	/**
	 * Ponto de entrada da aplicação Java.
	 */
	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

}
