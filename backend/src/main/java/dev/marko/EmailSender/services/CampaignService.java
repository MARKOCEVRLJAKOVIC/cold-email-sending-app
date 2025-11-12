package dev.marko.EmailSender.services;

import dev.marko.EmailSender.auth.AuthService;
import dev.marko.EmailSender.dtos.CampaignDto;
import dev.marko.EmailSender.dtos.CampaignStatsDto;
import dev.marko.EmailSender.dtos.CreateCampaignRequest;
import dev.marko.EmailSender.entities.Status;
import dev.marko.EmailSender.exception.CampaignNotFoundException;
import dev.marko.EmailSender.mappers.CampaignMapper;
import dev.marko.EmailSender.repositories.CampaignRepository;
import dev.marko.EmailSender.repositories.EmailMessageRepository;
import dev.marko.EmailSender.security.CurrentUserProvider;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@AllArgsConstructor
@Service
public class CampaignService {

    private final CampaignRepository campaignRepository;
    private final CampaignMapper campaignMapper;
    private final CurrentUserProvider currentUserProvider;
    private final EmailMessageRepository emailMessageRepository;


    public List<CampaignDto> getAllCampaignFromUser(){

        var user = currentUserProvider.getCurrentUser();

        var campaignList = campaignRepository.findAllByUserId(user.getId());

        return campaignList.stream()
                .map(campaign -> new CampaignDto(
                        campaign.getId(),
                        campaign.getDescription(),
                        campaign.getName(),
                        campaign.getCreatedAt(),
                        campaign.getUser() != null ? campaign.getUser().getId() : null)
                ).toList();

    }

    public CampaignDto getCampaign(Long id){

        var campaign = campaignRepository.findById(id).orElseThrow(CampaignNotFoundException::new);

        return campaignMapper.toDto(campaign);

    }

    public CampaignStatsDto getCampaignStats(Long id){

        var campaign = campaignRepository.findById(id).orElseThrow(CampaignNotFoundException::new);

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

    public CampaignDto createCampaign(CreateCampaignRequest request){
        var user = currentUserProvider.getCurrentUser();

        var campaign = campaignMapper.toEntity(request);
        campaign.setUser(user);

        campaignRepository.save(campaign);

        var campaignDto = campaignMapper.toDto(campaign);
        campaignDto.setId(campaign.getId());
        campaignDto.setUserId(user.getId());

        return campaignDto;
    }

    public CampaignDto updateCampaign(Long id ,CreateCampaignRequest request){

        var user = currentUserProvider.getCurrentUser();

        var campaign = campaignRepository.findById(id).
                orElseThrow(CampaignNotFoundException::new);

        campaignMapper.update(request, campaign);

        campaignRepository.save(campaign);

        var campaignDto = campaignMapper.toDto(campaign);

        campaignDto.setUserId(user.getId());

        return campaignDto;

    }

    public void deleteCampaign(Long id){
        var campaign = campaignRepository.findById(id)
                .orElseThrow(CampaignNotFoundException::new);

        campaignRepository.delete(campaign);
    }
}
