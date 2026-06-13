package com.humanworkstream.cooked.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/** Sends plain-text notification emails. Falls back to logging when mail is unconfigured. */
@Slf4j
@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final String from;
    private final boolean enabled;
    private final String username;

    public EmailService(JavaMailSender mailSender,
                        @Value("${app.mail.from}") String from,
                        @Value("${app.mail.enabled}") boolean enabled,
                        @Value("${spring.mail.username:}") String username) {
        this.mailSender = mailSender;
        this.from = from;
        this.enabled = enabled;
        this.username = username;
    }

    public void send(String to, String subject, String body) {
        if (!enabled || username == null || username.isBlank()) {
            log.warn("[EmailService] mail disabled/unconfigured — would have sent to {} subject='{}':\n{}", to, subject, body);
            return;
        }
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom(from);
            msg.setTo(to);
            msg.setSubject(subject);
            msg.setText(body);
            mailSender.send(msg);
            log.info("[EmailService] sent '{}' to {}", subject, to);
        } catch (Exception e) {
            // Never fail the caller's request because email delivery hiccupped.
            log.error("[EmailService] failed to send to {}: {}", to, e.getMessage());
        }
    }
}
