package com.upkeep.application.port.out;

import com.upkeep.domain.model.customer.Password;
import com.upkeep.domain.model.customer.PasswordHash;

public interface PasswordHasher {

    PasswordHash hash(Password password);
    boolean verify(Password password, PasswordHash hash);
}
