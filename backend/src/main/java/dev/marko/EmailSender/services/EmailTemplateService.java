package dev.marko.EmailSender.services;

import dev.marko.EmailSender.auth.AuthService;
import dev.marko.EmailSender.dtos.CreateTemplateRequest;
import dev.marko.EmailSender.dtos.EmailTemplateDto;
import dev.marko.EmailSender.exception.CampaignNotFoundException;
import dev.marko.EmailSender.exception.TemplateEmptyListException;
import dev.marko.EmailSender.exception.TemplateNotFoundException;
import dev.marko.EmailSender.mappers.EmailTemplateMapper;
import dev.marko.EmailSender.repositories.CampaignRepository;
import dev.marko.EmailSender.repositories.TemplateRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@AllArgsConstructor
@Service
public class EmailTemplateService {

    private final TemplateRepository templateRepository;
    private final EmailTemplateMapper emailTemplateMapper;
    private final AuthService authService;
    private final CampaignRepository campaignRepository;

    public List<EmailTemplateDto> getAllTemplates() {

        var user = authService.getCurrentUser();

        var templates = templateRepository.findAllByUserId(user.getId());

        if (templates.isEmpty()) throw new TemplateEmptyListException();

        return templates.stream().map(
                emailTemplateMapper::toDto
        ).toList();

    }

    public EmailTemplateDto getTemplate(Long id){
        var user = authService.getCurrentUser();

        var emailTemplate = templateRepository.findByIdAndUserId(id,user.getId())
                .orElseThrow(TemplateNotFoundException::new);

        var emailTemplateDto = emailTemplateMapper.toDto(emailTemplate);

        return emailTemplateDto;
    }

    public EmailTemplateDto createTemplate(CreateTemplateRequest request){
        var user = authService.getCurrentUser();

        var campaign = campaignRepository.findById
                (request.getCampaignId()).orElseThrow(CampaignNotFoundException::new);

        var emailTemplate = emailTemplateMapper.toEntity(request);
        emailTemplate.setCampaign(campaign);
        emailTemplate.setUser(user);

        templateRepository.save(emailTemplate);

        var emailTemplateDto = emailTemplateMapper.toDto(emailTemplate);
        emailTemplateDto.setId(emailTemplate.getId());

        emailTemplateDto.setUserId(user.getId());

        return emailTemplateDto;
    }


    public EmailTemplateDto updateTemplate(CreateTemplateRequest request, Long id){

        var user = authService.getCurrentUser();

        var campaign = campaignRepository.findById
                (request.getCampaignId()).orElseThrow(CampaignNotFoundException::new);

        var emailTemplate = emailTemplateMapper.toEntity(request);
        emailTemplate.setId(id);
        emailTemplate.setUser(user);
        emailTemplate.setCampaign(campaign);

        emailTemplateMapper.update(request, emailTemplate);

        templateRepository.save(emailTemplate);

        var emailTemplateDto =  emailTemplateMapper.toDto(emailTemplate);
        emailTemplateDto.setUserId(user.getId());
        emailTemplateDto.setCampaignId(campaign.getId());

        return emailTemplateDto;
    }

    public void deleteTemplate(Long id){
        var emailTemplate = templateRepository.findById(id).orElseThrow(TemplateNotFoundException::new);

        templateRepository.delete(emailTemplate);
    }
}
