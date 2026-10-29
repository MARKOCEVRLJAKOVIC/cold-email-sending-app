package dev.marko.EmailSender.metrics;

import dev.marko.EmailSender.entities.SmtpCredentials;
import dev.marko.EmailSender.entities.SmtpType;
import dev.marko.EmailSender.entities.Status;
import dev.marko.EmailSender.redis.RedisKeys;
import dev.marko.EmailSender.repositories.EmailMessageRepository;
import dev.marko.EmailSender.repositories.SmtpRepository;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.PendingMessagesSummary;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Health metrics for the sending pipeline, exposed at /actuator/prometheus.
 * Counters are recorded by the pipeline services; gauges are refreshed on a schedule
 * so a Prometheus scrape never hits Redis or MySQL directly.
 * Accounts are tagged by SMTP id only, never by email address.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EmailPipelineMetrics {

    private final MeterRegistry registry;
    private final StringRedisTemplate redis;
    private final EmailMessageRepository emailMessageRepository;
    private final SmtpRepository smtpRepository;

    private final AtomicLong scheduledBacklog = new AtomicLong();
    private final AtomicLong scheduledOverdue = new AtomicLong();
    private final AtomicLong streamPending = new AtomicLong();
    private final AtomicLong deadLetterSize = new AtomicLong();
    private final AtomicLong redisUp = new AtomicLong();
    private final AtomicLong processingEmails = new AtomicLong();
    private final AtomicLong disabledGmailAccounts = new AtomicLong();

    @PostConstruct
    void registerGauges() {
        gauge("email.scheduled.backlog", scheduledBacklog, "Emails waiting in the scheduled_emails ZSET");
        gauge("email.scheduled.overdue", scheduledOverdue, "Scheduled emails whose send time has passed");
        gauge("email.stream.pending", streamPending, "Stream entries read by a worker but not acknowledged");
        gauge("email.dead.letter.size", deadLetterSize, "Entries in the dead-letter stream");
        gauge("email.metrics.redis.up", redisUp, "1 if the last Redis gauge refresh succeeded");
        gauge("email.processing", processingEmails, "Emails currently in PROCESSING status");
        gauge("smtp.accounts.disabled", disabledGmailAccounts, "Disabled Gmail OAuth accounts");
    }

    private void gauge(String name, AtomicLong value, String description) {
        Gauge.builder(name, value, AtomicLong::get)
                .description(description)
                .register(registry);
    }

    public void recordSent(SmtpCredentials smtp) {
        registry.counter("email.sent", "account", accountTag(smtp)).increment();
    }

    public void recordFailed(SmtpCredentials smtp) {
        registry.counter("email.failed", "account", accountTag(smtp)).increment();
    }

    public void recordRateLimited(SmtpCredentials smtp) {
        registry.counter("email.rate.limited", "account", accountTag(smtp)).increment();
    }

    public void recordDeadLettered() {
        registry.counter("email.dead.lettered").increment();
    }

    public void recordRateLimiterFailOpen(String limiter) {
        registry.counter("rate.limiter.fail.open", "limiter", limiter).increment();
    }

    public void recordOAuthRevoked() {
        registry.counter("oauth.refresh.revoked").increment();
    }

    public void recordReplyMatched() {
        registry.counter("email.reply.matched").increment();
    }

    @Scheduled(fixedDelay = 30_000)
    public void refreshGauges() {
        refreshRedisGauges();
        refreshDatabaseGauges();
    }

    private void refreshRedisGauges() {
        try {
            long now = Instant.now().getEpochSecond();

            Long backlog = redis.opsForZSet().zCard(RedisKeys.SCHEDULED_EMAILS);
            Long overdue = redis.opsForZSet().count(RedisKeys.SCHEDULED_EMAILS, 0, now);
            Long deadLetter = redis.opsForStream().size(RedisKeys.DEAD_LETTER_STREAM);

            scheduledBacklog.set(backlog == null ? 0 : backlog);
            scheduledOverdue.set(overdue == null ? 0 : overdue);
            deadLetterSize.set(deadLetter == null ? 0 : deadLetter);
            streamPending.set(pendingInSendStream());
            redisUp.set(1);
        } catch (Exception e) {
            redisUp.set(0);
            log.warn("Failed to refresh Redis pipeline metrics: {}", e.getMessage());
        }
    }

    private long pendingInSendStream() {
        if (!Boolean.TRUE.equals(redis.hasKey(RedisKeys.SEND_STREAM))) {
            return 0;
        }
        PendingMessagesSummary summary = redis.opsForStream()
                .pending(RedisKeys.SEND_STREAM, RedisKeys.SEND_GROUP);
        return summary == null ? 0 : summary.getTotalPendingMessages();
    }

    private void refreshDatabaseGauges() {
        try {
            processingEmails.set(emailMessageRepository.countByStatus(Status.PROCESSING));
            disabledGmailAccounts.set(smtpRepository.countBySmtpTypeAndEnabled(SmtpType.GMAIL, false));
        } catch (Exception e) {
            log.warn("Failed to refresh database pipeline metrics: {}", e.getMessage());
        }
    }

    private static String accountTag(SmtpCredentials smtp) {
        return smtp == null || smtp.getId() == null ? "unknown" : smtp.getId().toString();
    }
}
