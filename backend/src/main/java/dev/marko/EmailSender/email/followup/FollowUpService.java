package dev.marko.EmailSender.email.followup;

import dev.marko.EmailSender.auth.AuthService;
import dev.marko.EmailSender.mappers.FollowUpMapper;
import dev.marko.EmailSender.repositories.CampaignRepository;
import dev.marko.EmailSender.repositories.FollowUpTemplateRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@AllArgsConstructor
@Service
public class FollowUpService {

    private final CampaignRepository campaignRepository;
    private final FollowUpTemplateRepository followUpTemplateRepository;
    private final AuthService authService;
    private final FollowUpMapper followUpMapper;

    public List<FollowUpDto> getAllFollowUps(){
        var user = authService.getCurrentUser();

        var followUpList = followUpTemplateRepository.findAllByUserId(user.getId());
        return followUpMapper.toListDto(followUpList);
    }

    public FollowUpDto getFollowUp(Long id){

        var user = authService.getCurrentUser();

        var followUp = followUpTemplateRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(FollowUpNotFoundException::new);

        return followUpMapper.toDto(followUp);

    }

    public FollowUpDto addFollowUpToCampaign(Long campaignId, CreateFollowUpRequest request){
        var user = authService.getCurrentUser();

        var campaign = campaignRepository.findById(campaignId)
                .filter(c -> c.getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new IllegalArgumentException("Campaign not found or it's not yours"));

        var followUp = followUpMapper.toEntity(request);
        followUp.setCampaign(campaign);
        followUp.setUser(user);
        followUpTemplateRepository.save(followUp);
        return followUpMapper.toDto(followUp);
    }

    public  FollowUpDto updateFollowUp(Long id, CreateFollowUpRequest request){
        var user = authService.getCurrentUser();

        var followUp = followUpTemplateRepository
                .findByIdAndUserId(id, user.getId()).orElseThrow(FollowUpNotFoundException::new);

        followUpMapper.update(request, followUp);
        followUpTemplateRepository.save(followUp);

        return followUpMapper.toDto(followUp);
    }

    public void deleteFollowUp(Long id){
        var user = authService.getCurrentUser();

        var followUp = followUpTemplateRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(FollowUpNotFoundException::new);

        followUpTemplateRepository.delete(followUp);
    }


}
