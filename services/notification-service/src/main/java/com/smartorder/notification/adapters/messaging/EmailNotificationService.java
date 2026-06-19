package com.smartorder.notification.adapters.messaging;

import com.smartorder.notification.adapters.persistence.NotificationLogRepository;
import com.smartorder.notification.domain.model.NotificationLog;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;

/**
 * Sends transactional emails and records each attempt
 * to the NotificationLog MongoDB collection.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailNotificationService {

    private final JavaMailSender mailSender;

    // ── Welcome email ─────────────────────────────────────────

    public void sendWelcomeEmail(
            String email,
            String fullName,
            Map<String, Object> event,
            NotificationLogRepository logRepo) {

        String subject = "Welcome to SmartOrder!";
        String body    = """
            Hi %s,

            Welcome to SmartOrder! We're excited to have you on board.

            Start exploring thousands of products from verified sellers.

            Best regards,
            The SmartOrder Team
            """.formatted(fullName != null ? fullName : "there");

        sendAndLog(email, subject, body,
                "USER_REGISTERED", (String) event.get("userId"), logRepo);
    }

    // ── Account locked email ──────────────────────────────────

    public void sendAccountLockedEmail(
            String email,
            Map<String, Object> event,
            NotificationLogRepository logRepo) {

        String subject = "Your SmartOrder account has been temporarily locked";
        String body    = """
            Hi,

            Your SmartOrder account has been temporarily locked due to
            multiple failed login attempts.

            Your account will be automatically unlocked after 30 minutes.

            If this wasn't you, please contact support immediately at
            support@smartorder.com.

            Best regards,
            The SmartOrder Security Team
            """;

        sendAndLog(email, subject, body,
                "ACCOUNT_LOCKED", (String) event.get("userId"), logRepo);
    }

    // ── Password changed email ────────────────────────────────

    public void sendPasswordChangedEmail(
            String email,
            Map<String, Object> event,
            NotificationLogRepository logRepo) {

        String subject = "Your SmartOrder password has been changed";
        String body    = """
            Hi,

            This is a confirmation that your SmartOrder password was
            successfully changed.

            If you did not make this change, please contact support
            immediately at support@smartorder.com.

            Best regards,
            The SmartOrder Security Team
            """;

        sendAndLog(email, subject, body,
                "PASSWORD_CHANGED", (String) event.get("userId"), logRepo);
    }

    // ── Internal send + log ───────────────────────────────────

    private void sendAndLog(String to,
                            String subject,
                            String body,
                            String eventType,
                            String userId,
                            NotificationLogRepository logRepo) {
        String status        = "SENT";
        String failureReason = null;

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            message.setFrom("noreply@smartorder.com");
            mailSender.send(message);
            log.info("Email sent: to={}, eventType={}", to, eventType);
        } catch (Exception e) {
            status        = "FAILED";
            failureReason = e.getMessage();
            log.error("Failed to send email: to={}, reason={}", to, e.getMessage());
        }

        try {
            logRepo.save(NotificationLog.builder()
                    .userId(userId)
                    .eventType(eventType)
                    .channel("EMAIL")
                    .recipient(to)
                    .subject(subject)
                    .body(body)
                    .status(status)
                    .failureReason(failureReason)
                    .occurredAt(Instant.now())
                    .processedAt(Instant.now())
                    .build());
        } catch (Exception e) {
            log.error("Failed to persist notification log: {}", e.getMessage());
        }
    }
}