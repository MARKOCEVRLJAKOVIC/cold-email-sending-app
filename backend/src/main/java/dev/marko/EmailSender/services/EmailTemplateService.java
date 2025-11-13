package dev.marko.EmailSender.services;

import dev.marko.EmailSender.dtos.CreateTemplateRequest;
import dev.marko.EmailSender.dtos.EmailTemplateDto;
import dev.marko.EmailSender.entities.EmailTemplate;
import dev.marko.EmailSender.exception.CampaignNotFoundException;
import dev.marko.EmailSender.exception.TemplateNotFoundException;
import dev.marko.EmailSender.mappers.EmailTemplateMapper;
import dev.marko.EmailSender.repositories.CampaignRepository;
import dev.marko.EmailSender.repositories.TemplateRepository;
import dev.marko.EmailSender.security.CurrentUserProvider;
import dev.marko.EmailSender.services.base.BaseService;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Getter
@Service
public class EmailTemplateService extends BaseService<EmailTemplate, EmailTemplateDto, CreateTemplateRequest, TemplateRepository> {


    private final EmailTemplateMapper emailTemplateMapper;
    private final CampaignRepository campaignRepository;

    public EmailTemplateService(
            TemplateRepository repository,
            CurrentUserProvider currentUserProvider,
            EmailTemplateMapper emailTemplateMapper,
            CampaignRepository campaignRepository
    ) {
        super(repository, currentUserProvider);
        this.emailTemplateMapper = emailTemplateMapper;
        this.campaignRepository = campaignRepository;
    }

    @Override
    protected EmailTemplateDto toDto(EmailTemplate entity) {
        return emailTemplateMapper.toDto(entity);
    }

    @Override
    protected EmailTemplate toEntity(CreateTemplateRequest request) {
        var user = currentUserProvider.getCurrentUser();
        var campaign = campaignRepository.findByIdAndUserId(request.getCampaignId(), user.getId())
                .orElseThrow(CampaignNotFoundException::new);
        var entity = emailTemplateMapper.toEntity(request);
        entity.setUser(user);
        entity.setCampaign(campaign);
        return entity;
    }

    @Override
    protected void updateEntity(EmailTemplate entity, CreateTemplateRequest request) {
        var user = currentUserProvider.getCurrentUser();
        var campaign = campaignRepository.findByIdAndUserId(request.getCampaignId(), user.getId())
                .orElseThrow(CampaignNotFoundException::new);
        emailTemplateMapper.update(request, entity);
        entity.setCampaign(campaign);
    }

    @Override
    protected RuntimeException notFoundException() {
        return new TemplateNotFoundException();
    }


}
