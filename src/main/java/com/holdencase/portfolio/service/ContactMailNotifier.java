package com.holdencase.portfolio.service;

import com.holdencase.portfolio.model.ContactMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class ContactMailNotifier {

    private static final Logger log = LoggerFactory.getLogger(ContactMailNotifier.class);

    private final JavaMailSender mailSender;
    private final String notifyTo;

    public ContactMailNotifier(JavaMailSender mailSender, @Value("${contact.notify-to:}") String notifyTo) {
        this.mailSender = mailSender;
        this.notifyTo = notifyTo;
    }

    /**
     * Runs on a background thread so a slow/unreachable mail server never blocks
     * the visitor's HTTP request — the contact form response doesn't wait on this.
     */
    @Async
    public void notify(ContactMessage saved) {
        if (notifyTo == null || notifyTo.isBlank()) {
            log.warn("Skipping contact email notification: contact.notify-to / MAIL_USERNAME is not configured");
            return;
        }
        try {
            SimpleMailMessage mail = new SimpleMailMessage();
            mail.setTo(notifyTo);
            mail.setSubject("New contact form message: " + saved.getSubject());
            mail.setText("""
                    From: %s <%s>

                    %s
                    """.formatted(saved.getName(), saved.getEmail(), saved.getMessage()));
            mailSender.send(mail);
        } catch (MailException e) {
            log.error("Failed to send contact notification email for message id {}", saved.getId(), e);
        }
    }
}
