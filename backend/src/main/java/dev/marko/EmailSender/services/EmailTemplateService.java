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
import dev.marko.EmailSender.security.CurrentUserProvider;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@AllArgsConstructor
@Service
public class EmailTemplateService {

    private final TemplateRepository templateRepository;
    private final EmailTemplateMapper emailTemplateMapper;
    private final CurrentUserProvider currentUserProvider;
    private final CampaignRepository campaignRepository;

    public List<EmailTemplateDto> getAllTemplates() {

        var user = currentUserProvider.getCurrentUser();
        var templates = templateRepository.findAllByUserId(user.getId());

        return emailTemplateMapper.toTemplateListDto(templates);

    }

    public EmailTemplateDto getTemplate(Long id){
        var user = currentUserProvider.getCurrentUser();
        var emailTemplate = templateRepository.findByIdAndUserId(id,user.getId())
                .orElseThrow(TemplateNotFoundException::new);

        return emailTemplateMapper.toDto(emailTemplate);
    }

    @Transactional
    public EmailTemplateDto createTemplate(CreateTemplateRequest request){
        var user = currentUserProvider.getCurrentUser();

        var campaign = campaignRepository.findByIdAndUserId(request.getCampaignId(), user.getId())
                .orElseThrow(CampaignNotFoundException::new);

        var emailTemplate = emailTemplateMapper.toEntity(request);
        emailTemplate.setCampaign(campaign);
        emailTemplate.setUser(user);

        templateRepository.save(emailTemplate);

        return emailTemplateMapper.toDto(emailTemplate);
    }

    @Transactional
    public EmailTemplateDto updateTemplate(CreateTemplateRequest request, Long id){

        var user = currentUserProvider.getCurrentUser();

        var campaign = campaignRepository.findById
                (request.getCampaignId()).orElseThrow(CampaignNotFoundException::new);

        var emailTemplate = emailTemplateMapper.toEntity(request);
        emailTemplate.setId(id);
        emailTemplate.setUser(user);
        emailTemplate.setCampaign(campaign);

        emailTemplateMapper.update(request, emailTemplate);

        templateRepository.save(emailTemplate);

        return emailTemplateMapper.toDto(emailTemplate);

    }

    @Transactional
    public void deleteTemplate(Long id){

        var user = currentUserProvider.getCurrentUser();
        var emailTemplate = templateRepository.findByIdAndUserId(id, user.getId()).orElseThrow(TemplateNotFoundException::new);

        templateRepository.delete(emailTemplate);

    }
}
