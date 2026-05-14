package com.tiagoagueda.api.admin;

import com.tiagoagueda.api.admin.dto.AdminUserDTO;
import com.tiagoagueda.api.admin.dto.UpdatePlanRequest;
import com.tiagoagueda.api.auth.repository.PasswordResetTokenRepository;
import com.tiagoagueda.api.auth.repository.RefreshTokenRepository;
import com.tiagoagueda.api.journal.repository.DailyEntryRepository;
import com.tiagoagueda.api.user.AppUser;
import com.tiagoagueda.api.user.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final AppUserRepository userRepository;
    private final DailyEntryRepository dailyEntryRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;

    public List<AdminUserDTO> listAllUsers() {
        return userRepository.findAll().stream()
                .map(this::toAdminDTO)
                .toList();
    }

    @Transactional
    public void deleteUser(UUID userId) {
        AppUser user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("Utilizador não encontrado."));

        passwordResetTokenRepository.deleteAllByUser(user);
        refreshTokenRepository.deleteAllByUser(user);
        dailyEntryRepository.deleteAllByAppUser(user);
        userRepository.delete(user);
    }

    @Transactional
    public AdminUserDTO updatePlan(UUID userId, UpdatePlanRequest request) {
        AppUser user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("Utilizador não encontrado."));
        user.setPlan(request.plan());
        return toAdminDTO(userRepository.save(user));
    }

    private AdminUserDTO toAdminDTO(AppUser user) {
        long totalEntries = dailyEntryRepository.countByAppUser(user);
        return new AdminUserDTO(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getPlan(),
                user.getGoalSetAt(),
                totalEntries
        );
    }
}
