package com.beta.expedition.service;

import com.beta.expedition.model.AppUser;
import com.beta.expedition.model.AuthenticatedUser;
import com.beta.expedition.model.Role;
import com.beta.expedition.model.enums.RoleName;
import com.beta.expedition.repository.AppUserRepository;
import com.beta.expedition.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
public class AuthService {

    private static final Pattern NICKNAME_PATTERN = Pattern.compile("[A-Za-z0-9_.-]{3,30}");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    private static final int MIN_PASSWORD_LENGTH = 6;
    private static final int MAX_PASSWORD_BYTES = 72; // ограничение BCrypt
    private static final int MAX_EMAIL_LENGTH = 150;

    private static final Set<RoleName> SELF_REGISTRATION_ROLES = Set.of(RoleName.CUSTOMER, RoleName.CARRIER);

    private final AppUserRepository users;
    private final RoleRepository roles;
    private final PasswordEncoder passwordEncoder;
    private final String adminUsername;
    private final String adminPassword;

    public AuthService(AppUserRepository users,
                       RoleRepository roles,
                       PasswordEncoder passwordEncoder,
                       @Value("${app.admin.username}") String adminUsername,
                       @Value("${app.admin.password}") String adminPassword) {
        this.users = users;
        this.roles = roles;
        this.passwordEncoder = passwordEncoder;
        this.adminUsername = adminUsername;
        this.adminPassword = adminPassword;
    }

    public AuthenticatedUser register(String nickname, String email, String password, RoleName role) {
        if (isAdminCredentials(nickname, password)) {
            return adminSession();
        }
        if (!SELF_REGISTRATION_ROLES.contains(role)) {
            throw new AuthException("This role is not available for registration");
        }
        UUID id = createUser(nickname, email, password, role);
        return new AuthenticatedUser(id, nickname, role);
    }

    public AuthenticatedUser login(String nickname, String password) {
        if (isAdminCredentials(nickname, password)) {
            return adminSession();
        }
        AppUser user = users.findByFullNameIgnoreCase(nickname).orElse(null);
        if (user == null
                || !user.isActive()
                || !passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new AuthException("Invalid nickname or password");
        }
        RoleName roleName = resolveRoleName(user.getRoleId());
        return new AuthenticatedUser(user.getId(), user.getFullName(), roleName);
    }

    public void createForwarder(AuthenticatedUser actor, String nickname, String email, String password) {
        if (actor == null || actor.role() != RoleName.ADMIN) {
            throw new AuthException("Access denied");
        }
        createUser(nickname, email, password, RoleName.FORWARDER);
    }

    private UUID createUser(String nickname, String email, String password, RoleName roleName) {
        if (!NICKNAME_PATTERN.matcher(nickname).matches()) {
            throw new AuthException("Nickname must be 3-30 characters: letters, digits, '_', '.', '-'");
        }
        if (email.length() > MAX_EMAIL_LENGTH || !EMAIL_PATTERN.matcher(email).matches()) {
            throw new AuthException("Invalid email");
        }
        if (password.length() < MIN_PASSWORD_LENGTH) {
            throw new AuthException("Password must be at least " + MIN_PASSWORD_LENGTH + " characters");
        }
        if (password.getBytes(StandardCharsets.UTF_8).length > MAX_PASSWORD_BYTES) {
            throw new AuthException("Password is too long");
        }
        if (adminUsername.equalsIgnoreCase(nickname) || users.existsByFullNameIgnoreCase(nickname)) {
            throw new AuthException("This nickname is already taken");
        }
        String normalizedEmail = email.toLowerCase(Locale.ROOT);
        if (users.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new AuthException("This email is already registered");
        }

        Role role = roles.findByName(roleName)
                .orElseThrow(() -> new IllegalStateException("Role not found in DB: " + roleName));

        AppUser user = new AppUser();
        user.setFullName(nickname);
        user.setEmail(normalizedEmail);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setRoleId(role.getId());
        user.setActive(true);

        try {
            return users.save(user).getId();
        } catch (DataIntegrityViolationException e) {
            throw new AuthException("This nickname or email is already taken");
        }
    }

    private RoleName resolveRoleName(Short roleId) {
        return roles.findAll().stream()
                .filter(r -> r.getId().equals(roleId))
                .map(Role::getName)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Role not found for id: " + roleId));
    }

    private boolean isAdminCredentials(String nickname, String password) {
        if (adminPassword == null || adminPassword.isBlank()) {
            return false;
        }
        boolean nicknameMatches = adminUsername.equalsIgnoreCase(nickname);
        boolean passwordMatches = MessageDigest.isEqual(
                adminPassword.getBytes(StandardCharsets.UTF_8),
                password.getBytes(StandardCharsets.UTF_8));
        return nicknameMatches && passwordMatches;
    }

    private AuthenticatedUser adminSession() {
        return new AuthenticatedUser(null, adminUsername, RoleName.ADMIN);
    }
}