package com.tiagoagueda.api.core.email;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Implementação de desenvolvimento que regista o email no log em vez de o enviar.
 *
 * Para produção, injeta um JavaMailSender e cria uma SmtpEmailService anotada
 * com @Primary @ConditionalOnProperty(name="spring.mail.host").
 */
@Service
public class LogEmailService implements EmailService {

    private static final Logger log = LoggerFactory.getLogger(LogEmailService.class);

    @Override
    public void sendPasswordResetEmail(String to, String resetLink) {
        log.info("=================================================================");
        log.info("  EMAIL DE RESET (DEV MODE - nao enviado por SMTP)");
        log.info("  Para: {}", to);
        log.info("  Link: {}", resetLink);
        log.info("  Expira em: 15 minutos");
        log.info("=================================================================");
    }
}
