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

    Long VALID_ID = 1L;
    Long INVALID_ID = 99L;

    @BeforeEach
    void setup(){

        user = new User();
        user.setId(VALID_ID);

        emailTemplate = new EmailTemplate();
        emailTemplate.setUser(user);
        emailTemplate.setId(VALID_ID);


        campaign = new Campaign();
        campaign.setUser(user);
        campaign.setId(VALID_ID);

        emailTemplateDto = new EmailTemplateDto();
        emailTemplateDto.setId(VALID_ID);
        emailTemplateDto.setUserId(user.getId());

        when(currentUserProvider.getCurrentUser()).thenReturn(user);


//        when(templateRepository.findByIdAndUserId(emailTemplate.getId(), user.getId())).thenReturn(Optional.of(emailTemplate));
//        when(campaignRepository.findById(campaign.getId())).thenReturn(Optional.of(campaign));
    }

    @Test
    void getAllTemplates_ShouldReturnListOfDtos(){

        when(templateRepository.findAllByUserId(VALID_ID)).thenReturn(List.of(emailTemplate));
        when(emailTemplateMapper.toDto(emailTemplate)).thenReturn(emailTemplateDto);

        var result = emailTemplateService.getAll();

        assertEquals(1, result.size());
        assertEquals(VALID_ID, result.get(0).getId());
        verify(templateRepository).findAllByUserId(VALID_ID);

    }

    @Test
    void getTemplate_ShouldReturnDto(){

        when(templateRepository.findByIdAndUserId(VALID_ID,VALID_ID)).thenReturn(Optional.of(emailTemplate));
        when(emailTemplateMapper.toDto(emailTemplate)).thenReturn(emailTemplateDto);

        var result = emailTemplateService.getById(VALID_ID);

        assertEquals(VALID_ID, result.getId());
        verify(templateRepository).findByIdAndUserId(VALID_ID, VALID_ID);

    }

    @Test
    void getTemplate_ShouldThrowNotFound() {

        when(templateRepository.findByIdAndUserId(INVALID_ID, VALID_ID)).thenReturn(Optional.empty());
        assertThrows(TemplateNotFoundException.class, () -> emailTemplateService.getById(INVALID_ID));

    }

    @Test
    void createTemplate_shouldSaveAndReturnDto() {

        CreateTemplateRequest request = new CreateTemplateRequest();
        request.setCampaignId(VALID_ID);

        when(campaignRepository.findByIdAndUserId(VALID_ID,VALID_ID)).thenReturn(Optional.of(campaign));
        when(emailTemplateMapper.toEntity(request)).thenReturn(emailTemplate);
        when(emailTemplateMapper.toDto(emailTemplate)).thenReturn(emailTemplateDto);

        var result = emailTemplateService.create(request);

        verify(templateRepository).save(emailTemplate);
        assertEquals(VALID_ID, result.getUserId());

    }

    @Test
    void createTemplate_ShouldThrowNotFound() {

        CreateTemplateRequest request = new CreateTemplateRequest();
        request.setCampaignId(INVALID_ID);

        when(campaignRepository.findByIdAndUserId(INVALID_ID, VALID_ID)).thenReturn(Optional.empty());

        assertThrows(CampaignNotFoundException.class,
                () -> emailTemplateService.create(request));
    }


    @Test
    void deleteTemplate_shouldDeleteWhenFound() {

        when(templateRepository.findByIdAndUserId(VALID_ID, VALID_ID)).thenReturn(Optional.of(emailTemplate));
        emailTemplateService.delete(VALID_ID);

        verify(templateRepository).delete(emailTemplate);

    }

    @Test
    void deleteTemplate_shouldThrowWhenNotFound() {
        when(templateRepository.findByIdAndUserId(INVALID_ID, VALID_ID)).thenReturn(Optional.empty());

        assertThrows(TemplateNotFoundException.class,
                () -> emailTemplateService.delete(INVALID_ID));
    }
}
