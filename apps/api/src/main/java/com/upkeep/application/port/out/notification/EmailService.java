package com.upkeep.application.port.out.notification;

import com.upkeep.domain.model.customer.Email;

public interface EmailService {
    void sendWelcomeEmail(Email email);

    void sendInvitationEmail(Email email, String invitationToken);
}
