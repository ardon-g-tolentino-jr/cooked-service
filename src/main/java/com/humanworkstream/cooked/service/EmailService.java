package com.humanworkstream.cooked.service;

import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

/**
 * Sends HTML notification emails rendered from Thymeleaf templates (mirrors the
 * appointments-api approach). Falls back to logging the temporary password when mail is
 * disabled or unconfigured, and never throws — delivery hiccups must not fail the caller's
 * request (registration / password reset still succeed).
 */
@Slf4j
@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;
    private final String from;
    private final boolean enabled;
    private final String username;
    private final String uiBaseUrl;

    public EmailService(JavaMailSender mailSender,
                        SpringTemplateEngine templateEngine,
                        @Value("${app.mail.from}") String from,
                        @Value("${app.mail.enabled}") boolean enabled,
                        @Value("${spring.mail.username:}") String username,
                        @Value("${app.ui.base-url}") String uiBaseUrl) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
        this.from = from;
        this.enabled = enabled;
        this.username = username;
        this.uiBaseUrl = uiBaseUrl;
    }

    /** Welcome email with the temporary password issued at registration. */
    public void sendWelcomeEmail(String to, String name, String tmpPassword) {
        Context ctx = new Context();
        ctx.setVariable("name", name);
        ctx.setVariable("tmpPassword", tmpPassword);
        ctx.setVariable("loginUrl", uiBaseUrl);
        sendHtml(to, "Welcome to Cooked — your temporary password", "welcome-email", ctx, tmpPassword);
    }

    /** Password-reset email with a single-use temporary password. */
    public void sendPasswordResetEmail(String to, String name, String tmpPassword) {
        Context ctx = new Context();
        ctx.setVariable("name", name);
        ctx.setVariable("tmpPassword", tmpPassword);
        ctx.setVariable("loginUrl", uiBaseUrl);
        sendHtml(to, "Your Cooked temporary password", "password-reset", ctx, tmpPassword);
    }

    private void sendHtml(String to, String subject, String template, Context ctx, String tmpPassword) {
        if (!enabled || username == null || username.isBlank()) {
            log.warn("[EmailService] mail disabled/unconfigured — would have sent '{}' to {} (temp password: {})",
                    subject, to, tmpPassword);
            return;
        }
        try {
            String html = templateEngine.process(template, ctx);
            MimeMessage mime = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mime, true, "UTF-8");
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true); // true → HTML body
            mailSender.send(mime);
            log.info("[EmailService] sent '{}' to {}", subject, to);
        } catch (Exception e) {
            // Never fail the caller's request because email delivery hiccupped.
            log.error("[EmailService] failed to send '{}' to {}: {}", subject, to, e.getMessage());
        }
    }
}
