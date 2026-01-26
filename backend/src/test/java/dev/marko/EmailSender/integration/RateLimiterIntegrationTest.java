package dev.marko.EmailSender.integration;

import dev.marko.EmailSender.entities.User;
import dev.marko.EmailSender.exception.RateLimitExceededException;
import dev.marko.EmailSender.ratelimit.RateLimitConfig;
import dev.marko.EmailSender.ratelimit.UserRateLimiter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@SpringBootTest
@ActiveProfiles("test")
public class RateLimiterIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private UserRateLimiter userRateLimiter;

    @Autowired
    private RateLimitConfig config;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
    }

    @Test
    @DisplayName("Should block batch requests after reaching limit")
    void testBatchRateLimiting() {
        int limit = config.getUser().getBatchRequestsPerHour();

        for (int i = 0; i < limit; i++) {
            assertDoesNotThrow(() -> userRateLimiter.consumeBatchOrThrow(testUser));
        }

        assertThrows(RateLimitExceededException.class, () ->
                userRateLimiter.consumeBatchOrThrow(testUser)
        );
    }

    @Test
    @DisplayName("Should block emails when quota is exceeded")
    void testEmailQuotaLimiting() {
        int limit = config.getUser().getEmailsPerHour();

        userRateLimiter.consumeEmailsOrThrow(testUser, limit - 10);

        assertThrows(RateLimitExceededException.class, () ->
                userRateLimiter.consumeEmailsOrThrow(testUser, 15)
        );
    }
}