package com.beta.expedition.service;

import com.beta.expedition.exception.BusinessException;
import com.beta.expedition.exception.EntityNotFoundException;
import com.beta.expedition.model.Role;
import com.beta.expedition.model.User;
import com.beta.expedition.repository.UserRepository;
import org.mindrot.jbcrypt.BCrypt;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public class AuthService {

    private static final Pattern NICKNAME = Pattern.compile("[\\p{L}\\p{N}_.-]{3,30}");
    private static final Pattern EMAIL = Pattern.compile("[^@\\s]+@[^@\\s]+\\.[^@\\s]+");
    private static final int MAX_EMAIL_LENGTH = 150;
    private static final int MIN_PASSWORD_LENGTH = 6;
    private static final int MAX_PASSWORD_BYTES = 72; // ограничение BCrypt
    private static final Set<Role> SELF_REGISTRATION_ROLES = Set.of(Role.CUSTOMER, Role.CARRIER);

    private final UserRepository users;

    public AuthService(UserRepository users) {
        this.users = users;
    }

    public User register(String nickname, String email, String password, Role role) {
        if (!SELF_REGISTRATION_ROLES.contains(role)) {
            throw new BusinessException("Эта роль недоступна для самостоятельной регистрации");
        }
        return createUser(nickname, email, password, role);
    }

    public User login(String nickname, String password) {
        User user = users.findByNickname(nickname.trim()).orElse(null);
        if (user == null || !user.isActive() || !BCrypt.checkpw(password, user.getPasswordHash())) {
            throw new BusinessException("Неверный никнейм или пароль");
        }
        return user;
    }

    public User createForwarder(User actor, String nickname, String email, String password) {
        requireAdmin(actor);
        return createUser(nickname, email, password, Role.FORWARDER);
    }

    public List<User> listForwarders(User actor) {
        requireAdmin(actor);
        return users.findByRole(Role.FORWARDER);
    }

    public void removeForwarder(User actor, long forwarderId) {
        requireAdmin(actor);
        User forwarder = users.findById(forwarderId)
                .orElseThrow(() -> new EntityNotFoundException("Экспедитор", forwarderId));
        if (forwarder.getRole() != Role.FORWARDER) {
            throw new BusinessException("Пользователь #" + forwarderId + " не является экспедитором");
        }
        if (!forwarder.isActive()) {
            throw new BusinessException("Экспедитор #" + forwarderId + " уже удалён");
        }
        users.setActive(forwarderId, false);
    }

    private User createUser(String nickname, String email, String password, Role role) {
        nickname = nickname.trim();
        email = email.trim();
        if (!NICKNAME.matcher(nickname).matches()) {
            throw new BusinessException("Никнейм: 3-30 символов (буквы, цифры, '_', '.', '-')");
        }
        if (email.length() > MAX_EMAIL_LENGTH || !EMAIL.matcher(email).matches()) {
            throw new BusinessException("Некорректный email");
        }
        if (password.length() < MIN_PASSWORD_LENGTH
                || password.getBytes(StandardCharsets.UTF_8).length > MAX_PASSWORD_BYTES) {
            throw new BusinessException("Пароль: от " + MIN_PASSWORD_LENGTH + " символов, не длиннее "
                    + MAX_PASSWORD_BYTES + " байт");
        }
        if (users.findByNickname(nickname).isPresent()) {
            throw new BusinessException("Никнейм '" + nickname + "' уже занят");
        }
        if (users.findByEmail(email).isPresent()) {
            throw new BusinessException("Email '" + email + "' уже зарегистрирован");
        }
        return users.save(new User(nickname, email, BCrypt.hashpw(password, BCrypt.gensalt()), role));
    }

    private void requireAdmin(User actor) {
        if (actor == null || actor.getRole() != Role.ADMIN) {
            throw new BusinessException("Доступ запрещён");
        }
    }
}
