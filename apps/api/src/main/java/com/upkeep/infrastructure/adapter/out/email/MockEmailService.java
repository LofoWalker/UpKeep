package com.upkeep.infrastructure.adapter.out.email;

import com.upkeep.application.port.out.notification.EmailService;
import com.upkeep.domain.model.customer.Email;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;

@ApplicationScoped
public class MockEmailService implements EmailService {
    private static final Logger LOG = Logger.getLogger(MockEmailService.class);

    @Override
    public void sendWelcomeEmail(Email email) {
        LOG.infof("📧 [MOCK] Sending welcome email to: %s", email.value());
        LOG.infof("📧 [MOCK] Subject: Welcome to Upkeep!");
        LOG.infof("📧 [MOCK] Body: Thank you for creating an account with Upkeep. We're excited to have you!");
    }
}