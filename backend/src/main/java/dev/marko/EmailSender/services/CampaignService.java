package dev.marko.EmailSender.services;

import dev.marko.EmailSender.dtos.CampaignDto;
import dev.marko.EmailSender.dtos.CampaignStatsDto;
import dev.marko.EmailSender.dtos.CreateCampaignRequest;
import dev.marko.EmailSender.dtos.UpdateCampaignRequest;
import dev.marko.EmailSender.entities.Campaign;
import dev.marko.EmailSender.entities.Status;
import dev.marko.EmailSender.entities.User;
import dev.marko.EmailSender.exception.CampaignNotFoundException;
import dev.marko.EmailSender.mappers.CampaignMapper;
import dev.marko.EmailSender.repositories.CampaignRepository;
import dev.marko.EmailSender.repositories.EmailMessageRepository;
import dev.marko.EmailSender.security.CurrentUserProvider;
import dev.marko.EmailSender.services.base.BaseService;
import org.springframework.stereotype.Service;

@Service
public class CampaignService extends BaseService<Campaign, CampaignDto, CreateCampaignRequest, CampaignRepository, UpdateCampaignRequest> {

    private final CampaignMapper campaignMapper;
    private final EmailMessageRepository emailMessageRepository;

    public CampaignService( CampaignRepository repository,
                            CurrentUserProvider currentUserProvider,
                            CampaignMapper campaignMapper,
                            EmailMessageRepository emailMessageRepository) {
        super(repository, currentUserProvider, CampaignNotFoundException::new);
        this.campaignMapper = campaignMapper;
        this.emailMessageRepository = emailMessageRepository;

    }

    @Override
    protected CampaignDto toDto(Campaign entity) {
        return campaignMapper.toDto(entity);
    }

    @Override
    protected Campaign toEntity(CreateCampaignRequest request) {
        return campaignMapper.toEntity(request);
    }

    @Override
    protected void updateEntity(Campaign entity, UpdateCampaignRequest request) {
        campaignMapper.update(request, entity);
    }

    @Override
    protected void setUser(Campaign entity, User user) {
        entity.setUser(user);
    }

    public CampaignStatsDto getCampaignStats(Long id){

        var user = currentUserProvider.getCurrentUser();

        var campaign = repository.findByIdAndUserId(id, user.getId())
                .orElseThrow(CampaignNotFoundException::new);
        var emails = emailMessageRepository.findAllByCampaign(campaign);

        int total = emails.size();
        int sent = (int) emails.stream()
                .filter(e -> e.getStatus() == Status.SENT || e.getStatus() == Status.REPLIED)
                .count();
        int failed = (int) emails.stream().filter(e -> e.getStatus() == Status.FAILED).count();
        int pending = (int) emails.stream().filter(e -> e.getStatus() == Status.PENDING).count();
        int replied = (int) emails.stream().filter(e -> e.getStatus() == Status.REPLIED).count();

        return new CampaignStatsDto(total, sent, failed, pending, replied);


    }


}
