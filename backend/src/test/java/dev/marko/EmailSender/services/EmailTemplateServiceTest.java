package dev.marko.EmailSender.services;

import dev.marko.EmailSender.auth.AuthService;
import dev.marko.EmailSender.dtos.EmailTemplateDto;
import dev.marko.EmailSender.entities.Campaign;
import dev.marko.EmailSender.entities.EmailTemplate;
import dev.marko.EmailSender.entities.User;
import dev.marko.EmailSender.mappers.EmailTemplateMapper;
import dev.marko.EmailSender.repositories.CampaignRepository;
import dev.marko.EmailSender.repositories.TemplateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class EmailTemplateServiceTest {

    @Mock private TemplateRepository templateRepository;
    @Mock private EmailTemplateMapper emailTemplateMapper;
    @Mock private AuthService authService;
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

//        when(templateRepository.findByIdAndUserId(emailTemplate.getId(), user.getId())).thenReturn(Optional.of(emailTemplate));
//        when(campaignRepository.findById(campaign.getId())).thenReturn(Optional.of(campaign));
    }

    @Test
    void getAllTemplates_ShouldReturnListOfDtos(){

        when(authService.getCurrentUser()).thenReturn(user);
        when(templateRepository.findAllByUserId(1L)).thenReturn(List.of(emailTemplate));
        when(emailTemplateMapper.toDto(emailTemplate)).thenReturn(emailTemplateDto);

        var result = emailTemplateService.getAllTemplates();

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
        verify(templateRepository).findAllByUserId(1L);

    }

}
