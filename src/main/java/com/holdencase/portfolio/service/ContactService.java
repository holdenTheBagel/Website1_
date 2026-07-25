package com.holdencase.portfolio.service;

import com.holdencase.portfolio.dto.ContactForm;
import com.holdencase.portfolio.model.ContactMessage;
import com.holdencase.portfolio.repository.ContactMessageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private final ContactMailNotifier mailNotifier;
    private final Map<String, Instant> lastSubmissionByIp = new ConcurrentHashMap<>();

    public ContactService(ContactMessageRepository repository, ContactMailNotifier mailNotifier) {
        this.repository = repository;
        this.mailNotifier = mailNotifier;
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

        mailNotifier.notify(saved);

        return Outcome.SAVED;
    }

    private boolean isRateLimited(String clientIp) {
        Instant last = lastSubmissionByIp.get(clientIp);
        return last != null && Duration.between(last, Instant.now()).compareTo(RATE_LIMIT_WINDOW) < 0;
    }
}
