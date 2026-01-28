package dev.marko.EmailSender.email.followup;

import dev.marko.EmailSender.entities.Campaign;
import dev.marko.EmailSender.entities.EmailMessage;
import dev.marko.EmailSender.entities.FollowUpTemplate;
import dev.marko.EmailSender.repositories.EmailMessageRepository;
import dev.marko.EmailSender.repositories.EmailReplyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FollowUpEligibilityService Tests")
public class FollowUpEligibilityServiceTest {

    @Mock
    private EmailMessageRepository emailMessageRepository;

    @Mock
    private EmailReplyRepository emailReplyRepository;

    private FollowUpEligibilityService eligibilityService;

    @BeforeEach
    void setUp() {
        eligibilityService = new FollowUpEligibilityService(
                emailMessageRepository,
                emailReplyRepository
        );
        // Manually set the field since @Value won't work in tests
        setMaximumFollowUps(eligibilityService, 2);
    }

    @Test
    @DisplayName("Should return null when email has reply")
    void shouldReturnNullWhenEmailHasReply() {
        // Given
        EmailMessage original = createOriginalEmail();
        when(emailReplyRepository.existsByEmailMessageId(original.getId()))
                .thenReturn(true);

        // When
        FollowUpTemplate result = eligibilityService.findEligibleTemplate(original);

        // Then
        assertThat(result).isNull();
        verify(emailReplyRepository).existsByEmailMessageId(original.getId());
    }

    @Test
    @DisplayName("Should return null when no templates in campaign")
    void shouldReturnNullWhenNoTemplates() {
        // Given
        EmailMessage original = createOriginalEmail();
        original.getCampaign().setFollowUpTemplates(List.of());

        when(emailReplyRepository.existsByEmailMessageId(original.getId()))
                .thenReturn(false);

        // When
        FollowUpTemplate result = eligibilityService.findEligibleTemplate(original);

        // Then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("Should return null when delay period not passed")
    void shouldReturnNullWhenDelayNotPassed() {
        // Given
        EmailMessage original = createOriginalEmail();
        original.setSentAt(LocalDateTime.now(ZoneId.of("UTC")).minusDays(1));

        FollowUpTemplate template = new FollowUpTemplate();
        template.setId(1L);
        template.setDelayDays(3); // Needs 3 days, only 1 day passed
        template.setTemplateOrder(1);

        original.getCampaign().setFollowUpTemplates(List.of(template));

        when(emailReplyRepository.existsByEmailMessageId(original.getId()))
                .thenReturn(false);

        // When
        FollowUpTemplate result = eligibilityService.findEligibleTemplate(original);

        // Then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("Should return null when follow-up already sent for template")
    void shouldReturnNullWhenFollowUpAlreadySent() {
        // Given
        EmailMessage original = createOriginalEmail();
        original.setSentAt(LocalDateTime.now(ZoneId.of("UTC")).minusDays(5));
        original.setMessageId("msg-123");

        FollowUpTemplate template = new FollowUpTemplate();
        template.setId(1L);
        template.setDelayDays(3);
        template.setTemplateOrder(1);

        original.getCampaign().setFollowUpTemplates(List.of(template));

        when(emailReplyRepository.existsByEmailMessageId(original.getId()))
                .thenReturn(false);
        when(emailMessageRepository.existsByInReplyToAndFollowUpTemplateId(
                "msg-123", 1L
        )).thenReturn(true);

        // When
        FollowUpTemplate result = eligibilityService.findEligibleTemplate(original);

        // Then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("Should return first eligible template based on order")
    void shouldReturnFirstEligibleTemplate() {
        // Given
        EmailMessage original = createOriginalEmail();
        original.setSentAt(LocalDateTime.now(ZoneId.of("UTC")).minusDays(10));
        original.setMessageId("msg-123");

        FollowUpTemplate template1 = createTemplate(1L, 2, 1);
        FollowUpTemplate template2 = createTemplate(2L, 5, 2);
        FollowUpTemplate template3 = createTemplate(3L, 8, 3);

        original.getCampaign().setFollowUpTemplates(List.of(template2, template1, template3));

        when(emailReplyRepository.existsByEmailMessageId(original.getId()))
                .thenReturn(false);
        when(emailMessageRepository.existsByInReplyToAndFollowUpTemplateId(
                eq("msg-123"), anyLong()
        )).thenReturn(false);

        // When
        FollowUpTemplate result = eligibilityService.findEligibleTemplate(original);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTemplateOrder()).isEqualTo(1);
    }

    @Test
    @DisplayName("Should skip first template if already sent and return second")
    void shouldSkipFirstAndReturnSecond() {
        // Given
        EmailMessage original = createOriginalEmail();
        original.setSentAt(LocalDateTime.now(ZoneId.of("UTC")).minusDays(10));
        original.setMessageId("msg-123");

        FollowUpTemplate template1 = createTemplate(1L, 2, 1);
        FollowUpTemplate template2 = createTemplate(2L, 5, 2);

        original.getCampaign().setFollowUpTemplates(List.of(template1, template2));

        when(emailReplyRepository.existsByEmailMessageId(original.getId()))
                .thenReturn(false);
        when(emailMessageRepository.existsByInReplyToAndFollowUpTemplateId("msg-123", 1L))
                .thenReturn(true); // First already sent
        when(emailMessageRepository.existsByInReplyToAndFollowUpTemplateId("msg-123", 2L))
                .thenReturn(false); // Second not sent

        // When
        FollowUpTemplate result = eligibilityService.findEligibleTemplate(original);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(2L);
    }

    @Test
    @DisplayName("Should respect maximum follow-ups limit")
    void shouldRespectMaximumLimit() {
        // Given
        EmailMessage original = createOriginalEmail();
        original.setSentAt(LocalDateTime.now(ZoneId.of("UTC")).minusDays(20));
        original.setMessageId("msg-123");

        FollowUpTemplate template1 = createTemplate(1L, 2, 1);
        FollowUpTemplate template2 = createTemplate(2L, 5, 2);
        FollowUpTemplate template3 = createTemplate(3L, 8, 3); // This should be ignored (limit=2)

        original.getCampaign().setFollowUpTemplates(List.of(template1, template2, template3));

        when(emailReplyRepository.existsByEmailMessageId(original.getId()))
                .thenReturn(false);
        when(emailMessageRepository.existsByInReplyToAndFollowUpTemplateId(
                eq("msg-123"), anyLong()
        )).thenReturn(true);

        // When
        FollowUpTemplate result = eligibilityService.findEligibleTemplate(original);

        // Then
        assertThat(result).isNull(); // All allowed templates (1,2) already sent
        verify(emailMessageRepository, times(2))
                .existsByInReplyToAndFollowUpTemplateId(eq("msg-123"), anyLong());
    }

    private EmailMessage createOriginalEmail() {
        EmailMessage email = new EmailMessage();
        email.setId(1L);
        email.setMessageId("msg-123");
        email.setSentAt(LocalDateTime.now(ZoneId.of("UTC")).minusDays(5));

        Campaign campaign = new Campaign();
        campaign.setFollowUpTemplates(new ArrayList<>());
        email.setCampaign(campaign);

        return email;
    }

    private FollowUpTemplate createTemplate(Long id, int delayDays, int order) {
        FollowUpTemplate template = new FollowUpTemplate();
        template.setId(id);
        template.setDelayDays(delayDays);
        template.setTemplateOrder(order);
        template.setMessage("Follow-up message " + id);
        return template;
    }

    private void setMaximumFollowUps(FollowUpEligibilityService service, int value) {
        try {
            var field = FollowUpEligibilityService.class.getDeclaredField("maximumFollowUps");
            field.setAccessible(true);
            field.set(service, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}