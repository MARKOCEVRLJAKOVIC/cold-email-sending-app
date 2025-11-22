package dev.marko.EmailSender.services;

import dev.marko.EmailSender.dtos.CreateTemplateRequest;
import dev.marko.EmailSender.dtos.EmailTemplateDto;
import dev.marko.EmailSender.dtos.UpdateTemplateRequest;
import dev.marko.EmailSender.entities.EmailTemplate;
import dev.marko.EmailSender.entities.User;
import dev.marko.EmailSender.exception.CampaignNotFoundException;
import dev.marko.EmailSender.exception.TemplateNotFoundException;
import dev.marko.EmailSender.mappers.EmailTemplateMapper;
import dev.marko.EmailSender.repositories.CampaignRepository;
import dev.marko.EmailSender.repositories.TemplateRepository;
import dev.marko.EmailSender.security.CurrentUserProvider;
import dev.marko.EmailSender.services.base.BaseService;
import org.springframework.stereotype.Service;

@Service
public class EmailTemplateService extends BaseService<
        EmailTemplate,
        EmailTemplateDto,
        CreateTemplateRequest,
        TemplateRepository,
        UpdateTemplateRequest
        > {

    private final EmailTemplateMapper mapper;
    private final CampaignRepository campaignRepository;

    public EmailTemplateService(
            TemplateRepository repository,
            CurrentUserProvider currentUserProvider,
            EmailTemplateMapper mapper,
            CampaignRepository campaignRepository
    ) {
        super(
                repository,
                currentUserProvider,
                TemplateNotFoundException::new
        );
        this.mapper = mapper;
        this.campaignRepository = campaignRepository;
    }

    @Override
    protected EmailTemplateDto toDto(EmailTemplate entity) {
        return mapper.toDto(entity);
    }

    @Override
    protected EmailTemplate toEntity(CreateTemplateRequest req) {
        var user = currentUserProvider.getCurrentUser();
        var campaign = campaignRepository.findByIdAndUserId(req.getCampaignId(), user.getId())
                .orElseThrow(CampaignNotFoundException::new);

        var entity = mapper.toEntity(req);
        entity.setCampaign(campaign);
        return entity;
    }

    @Override
    protected void updateEntity(EmailTemplate entity, UpdateTemplateRequest request) {
        mapper.update(request, entity);
    }


    @Override
    protected void setUser(EmailTemplate entity, User user) {
        entity.setUser(user);
    }
}
