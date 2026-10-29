package dev.marko.EmailSender.metrics;

import dev.marko.EmailSender.entities.SmtpCredentials;
import dev.marko.EmailSender.entities.SmtpType;
import dev.marko.EmailSender.entities.Status;
import dev.marko.EmailSender.redis.RedisKeys;
import dev.marko.EmailSender.repositories.EmailMessageRepository;
import dev.marko.EmailSender.repositories.SmtpRepository;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.StreamOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmailPipelineMetricsTest {

    @Mock
    private StringRedisTemplate redis;

    @Mock
    private ZSetOperations<String, String> zSetOps;

    @Mock
    private StreamOperations<String, Object, Object> streamOps;

    @Mock
    private EmailMessageRepository emailMessageRepository;

    @Mock
    private SmtpRepository smtpRepository;

    private SimpleMeterRegistry registry;
    private EmailPipelineMetrics metrics;

    @BeforeEach
    void setUp() {
        registry = new SimpleMeterRegistry();
        metrics = new EmailPipelineMetrics(registry, redis, emailMessageRepository, smtpRepository);
        metrics.registerGauges();
    }

    @Test
    void shouldTagSentCounterBySmtpId() {
        SmtpCredentials smtp = new SmtpCredentials();
        smtp.setId(7L);

        metrics.recordSent(smtp);
        metrics.recordSent(smtp);

        assertThat(registry.get("email.sent").tag("account", "7").counter().count()).isEqualTo(2.0);
    }

    @Test
    void shouldRefreshGaugesFromRedisAndDatabase() {
        when(redis.opsForZSet()).thenReturn(zSetOps);
        when(redis.<Object, Object>opsForStream()).thenReturn(streamOps);
        when(zSetOps.zCard(RedisKeys.SCHEDULED_EMAILS)).thenReturn(12L);
        when(zSetOps.count(eq(RedisKeys.SCHEDULED_EMAILS), eq(0.0), anyDouble())).thenReturn(3L);
        when(streamOps.size(RedisKeys.DEAD_LETTER_STREAM)).thenReturn(2L);
        when(redis.hasKey(RedisKeys.SEND_STREAM)).thenReturn(false);
        when(emailMessageRepository.countByStatus(Status.PROCESSING)).thenReturn(4L);
        when(smtpRepository.countBySmtpTypeAndEnabled(SmtpType.GMAIL, false)).thenReturn(1L);

        metrics.refreshGauges();

        assertThat(gauge("email.scheduled.backlog")).isEqualTo(12.0);
        assertThat(gauge("email.scheduled.overdue")).isEqualTo(3.0);
        assertThat(gauge("email.dead.letter.size")).isEqualTo(2.0);
        assertThat(gauge("email.stream.pending")).isEqualTo(0.0);
        assertThat(gauge("email.metrics.redis.up")).isEqualTo(1.0);
        assertThat(gauge("email.processing")).isEqualTo(4.0);
        assertThat(gauge("smtp.accounts.disabled")).isEqualTo(1.0);
    }

    @Test
    void shouldReportRedisDownAndStillRefreshDatabaseGauges() {
        when(redis.opsForZSet()).thenThrow(new RedisConnectionFailureException("down"));
        when(emailMessageRepository.countByStatus(Status.PROCESSING)).thenReturn(5L);

        metrics.refreshGauges();

        assertThat(gauge("email.metrics.redis.up")).isEqualTo(0.0);
        assertThat(gauge("email.processing")).isEqualTo(5.0);
    }

    private double gauge(String name) {
        return registry.get(name).gauge().value();
    }
}
