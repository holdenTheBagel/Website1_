package com.holdencase.portfolio.service;

import com.holdencase.portfolio.dto.ContactForm;
import com.holdencase.portfolio.model.ContactMessage;
import com.holdencase.portfolio.repository.ContactMessageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ContactService {

    private static final Logger log = LoggerFactory.getLogger(ContactService.class);
    private static final Duration RATE_LIMIT_WINDOW = Duration.ofSeconds(30);

    private final ContactMessageRepository repository;
    private final JavaMailSender mailSender;
    private final String notifyTo;
    private final Map<String, Instant> lastSubmissionByIp = new ConcurrentHashMap<>();

    public ContactService(ContactMessageRepository repository,
                          JavaMailSender mailSender,
                          @Value("${contact.notify-to:}") String notifyTo) {
        this.repository = repository;
        this.mailSender = mailSender;
        this.notifyTo = notifyTo;
    }

    public enum Outcome {
        SAVED, SPAM_REJECTED, RATE_LIMITED
    }

    public Outcome submit(ContactForm form, String clientIp) {
        if (form.getWebsite() != null && !form.getWebsite().isBlank()) {
            log.info("Rejected contact submission from {} — honeypot field was filled", clientIp);
            return Outcome.SPAM_REJECTED;
        }

        if (isRateLimited(clientIp)) {
            log.info("Rejected contact submission from {} — rate limited", clientIp);
            return Outcome.RATE_LIMITED;
        }

        ContactMessage saved = repository.save(new ContactMessage(
                form.getName(), form.getEmail(), form.getSubject(), form.getMessage()));
        lastSubmissionByIp.put(clientIp, Instant.now());

        sendNotificationEmail(saved);

        return Outcome.SAVED;
    }

    private boolean isRateLimited(String clientIp) {
        Instant last = lastSubmissionByIp.get(clientIp);
        return last != null && Duration.between(last, Instant.now()).compareTo(RATE_LIMIT_WINDOW) < 0;
    }

    private void sendNotificationEmail(ContactMessage saved) {
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
