package com.upkeep.application.port.out;

import com.upkeep.domain.model.customer.Email;

public interface EmailService {
    void sendWelcomeEmail(Email email);
}
