package com.tiagoagueda.api.admin;

import com.tiagoagueda.api.user.AppUser;
import com.tiagoagueda.api.user.AppUserRepository;
import com.tiagoagueda.api.user.Plan;
import com.tiagoagueda.api.user.Role;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminDataInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminDataInitializer.class);

    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${application.admin.email}")
    private String adminEmail;

    @Value("${application.admin.password}")
    private String adminPassword;

    @Override
    public void run(ApplicationArguments args) {
        if (userRepository.findByEmail(adminEmail).isEmpty()) {
            AppUser admin = AppUser.builder()
                    .name("Admin")
                    .email(adminEmail)
                    .password(passwordEncoder.encode(adminPassword))
                    .role(Role.ADMIN)
                    .plan(Plan.PRO)
                    .build();
            userRepository.save(admin);
            log.info("Conta admin criada com sucesso: {}", adminEmail);
        } else {
            log.debug("Conta admin já existe: {}", adminEmail);
        }
    }
}
