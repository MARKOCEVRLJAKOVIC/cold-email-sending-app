package dev.marko.EmailSender.services;

import dev.marko.EmailSender.dtos.CreateTemplateRequest;
import dev.marko.EmailSender.dtos.EmailTemplateDto;
import dev.marko.EmailSender.entities.Campaign;
import dev.marko.EmailSender.entities.EmailTemplate;
import dev.marko.EmailSender.entities.User;
import dev.marko.EmailSender.exception.CampaignNotFoundException;
import dev.marko.EmailSender.exception.TemplateNotFoundException;
import dev.marko.EmailSender.mappers.EmailTemplateMapper;
import dev.marko.EmailSender.repositories.CampaignRepository;
import dev.marko.EmailSender.repositories.TemplateRepository;
import dev.marko.EmailSender.security.CurrentUserProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class EmailTemplateServiceTest {

    @Mock private TemplateRepository templateRepository;
    @Mock private EmailTemplateMapper emailTemplateMapper;
    @Mock private CurrentUserProvider currentUserProvider;
    @Mock private CampaignRepository campaignRepository;

    @InjectMocks
    private EmailTemplateService emailTemplateService;

    User user;
    EmailTemplate emailTemplate;
    Campaign campaign;
    EmailTemplateDto emailTemplateDto;

    @BeforeEach
    void setup(){

        user = new User();
        user.setId(1L);

        emailTemplate = new EmailTemplate();
        emailTemplate.setUser(user);
        emailTemplate.setId(1L);


        campaign = new Campaign();
        campaign.setUser(user);
        campaign.setId(1L);

        emailTemplateDto = new EmailTemplateDto();
        emailTemplateDto.setId(1L);
        emailTemplateDto.setUserId(user.getId());

        when(currentUserProvider.getCurrentUser()).thenReturn(user);


//        when(templateRepository.findByIdAndUserId(emailTemplate.getId(), user.getId())).thenReturn(Optional.of(emailTemplate));
//        when(campaignRepository.findById(campaign.getId())).thenReturn(Optional.of(campaign));
    }

    @Test
    void getAllTemplates_ShouldReturnListOfDtos(){

        when(templateRepository.findAllByUserId(1L)).thenReturn(List.of(emailTemplate));
        when(emailTemplateMapper.toDto(emailTemplate)).thenReturn(emailTemplateDto);

        var result = emailTemplateService.getAllTemplates();

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
        verify(templateRepository).findAllByUserId(1L);

    }

    @Test
    void getTemplate_ShouldReturnDto(){

        when(templateRepository.findByIdAndUserId(1L,1L)).thenReturn(Optional.of(emailTemplate));
        when(emailTemplateMapper.toDto(emailTemplate)).thenReturn(emailTemplateDto);

        var result = emailTemplateService.getTemplate(1L);

        assertEquals(1L, result.getId());
        verify(templateRepository).findByIdAndUserId(1L, 1L);

    }

    @Test
    void getTemplate_ShouldThrowNotFound() {

        when(templateRepository.findByIdAndUserId(99L, 1L)).thenReturn(Optional.empty());
        assertThrows(TemplateNotFoundException.class, () -> emailTemplateService.getTemplate(99L));

    }

    @Test
    void createTemplate_shouldSaveAndReturnDto() {

        CreateTemplateRequest request = new CreateTemplateRequest();
        request.setCampaignId(1L);

        when(campaignRepository.findById(1L)).thenReturn(Optional.of(campaign));
        when(emailTemplateMapper.toEntity(request)).thenReturn(emailTemplate);
        when(emailTemplateMapper.toDto(emailTemplate)).thenReturn(emailTemplateDto);

        var result = emailTemplateService.createTemplate(request);

        verify(templateRepository).save(emailTemplate);
        assertEquals(1L, result.getUserId());

    }

    @Test
    void createTemplate_ShouldThrowNotFound() {

        CreateTemplateRequest request = new CreateTemplateRequest();
        request.setCampaignId(99L);

        when(campaignRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(CampaignNotFoundException.class,
                () -> emailTemplateService.createTemplate(request));
    }

    @Test
    void deleteTemplate_shouldDeleteWhenFound() {

        when(templateRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(emailTemplate));
        emailTemplateService.deleteTemplate(1L);

        verify(templateRepository).delete(emailTemplate);

    }

    @Test
    void deleteTemplate_shouldThrowWhenNotFound() {
        when(templateRepository.findByIdAndUserId(99L, 1L)).thenReturn(Optional.empty());

        assertThrows(TemplateNotFoundException.class,
                () -> emailTemplateService.deleteTemplate(99L));
    }
}
