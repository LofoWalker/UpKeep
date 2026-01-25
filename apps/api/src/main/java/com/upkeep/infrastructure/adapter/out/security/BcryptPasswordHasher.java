package com.upkeep.infrastructure.adapter.out.security;

import com.upkeep.application.port.out.auth.PasswordHasher;
import com.upkeep.domain.model.customer.Password;
import com.upkeep.domain.model.customer.PasswordHash;
import jakarta.enterprise.context.ApplicationScoped;
import org.mindrot.jbcrypt.BCrypt;

@ApplicationScoped
public class BcryptPasswordHasher implements PasswordHasher {
    private static final int COST_FACTOR = 12;

    @Override
    public PasswordHash hash(Password password) {
        String hashed = BCrypt.hashpw(password.value(), BCrypt.gensalt(COST_FACTOR));
        return new PasswordHash(hashed);
    }

    @Override
    public boolean verify(Password password, PasswordHash hash) {
        return BCrypt.checkpw(password.value(), hash.value());
    }
}