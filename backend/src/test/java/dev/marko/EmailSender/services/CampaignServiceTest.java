package dev.marko.EmailSender.services;

import dev.marko.EmailSender.dtos.CampaignDto;
import dev.marko.EmailSender.dtos.CreateCampaignRequest;
import dev.marko.EmailSender.entities.Campaign;
import dev.marko.EmailSender.entities.EmailMessage;
import dev.marko.EmailSender.entities.Status;
import dev.marko.EmailSender.entities.User;
import dev.marko.EmailSender.exception.CampaignNotFoundException;
import dev.marko.EmailSender.exception.EmailNotFoundException;
import dev.marko.EmailSender.mappers.CampaignMapper;
import dev.marko.EmailSender.repositories.CampaignRepository;
import dev.marko.EmailSender.repositories.EmailMessageRepository;
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
public class CampaignServiceTest {

    @Mock
    private CampaignRepository campaignRepository;
    @Mock
    private CampaignMapper campaignMapper;
    @Mock
    private CurrentUserProvider currentUserProvider;
    @Mock
    private EmailMessageRepository emailMessageRepository;

    @InjectMocks
    CampaignService campaignService;

    User user;
    Campaign campaign;
    CampaignDto campaignDto;

    Long VALID_ID = 1L;
    Long INVALID_ID = 99L;

    @BeforeEach
    void setup() {

        user = new User();
        user.setId(VALID_ID);

        campaign = new Campaign();
        campaign.setId(VALID_ID);
        campaign.setUser(user);

        campaignDto = new CampaignDto();
        campaignDto.setId(VALID_ID);

        when(currentUserProvider.getCurrentUser()).thenReturn(user);

    }

    @Test
    void getAllCampaignFromUser_ShouldReturnListOfUserDtos() {

        when(campaignRepository.findAllByUserId(user.getId())).thenReturn(List.of(campaign));
        when(campaignMapper.toDto(campaign)).thenReturn(campaignDto);

        var result = campaignService.getAll();

        assertEquals(1, result.size());
        assertEquals(VALID_ID, result.getFirst().getId());
        verify(campaignRepository).findAllByUserId(user.getId());

    }

    @Test
    void getCampaign_ShouldReturnDto() {

        when(campaignRepository.findByIdAndUserId(campaign.getId(), user.getId())).thenReturn(Optional.of(campaign));
        when(campaignMapper.toDto(campaign)).thenReturn(campaignDto);

        var result = campaignService.getById(campaign.getId());

        assertEquals(VALID_ID, result.getId());
        verify(campaignRepository).findByIdAndUserId(campaign.getId(), user.getId());

    }

    @Test
    void getCampaign_ShouldThrowException() {

        // throw exception by providing non-existing id
        when(campaignRepository.findByIdAndUserId(INVALID_ID, user.getId())).thenReturn(Optional.empty());
        assertThrows(CampaignNotFoundException.class, () -> campaignService.getById(INVALID_ID));

    }

    @Test
    void createCampaign_ShouldCreateNewCampaignAndReturnDto() {

        CreateCampaignRequest request = new CreateCampaignRequest();
        when(campaignMapper.toEntity(request)).thenReturn(campaign);
        when(campaignMapper.toDto(campaign)).thenReturn(campaignDto);

        var result = campaignService.create(request);

        verify(campaignRepository).save(campaign);
        assertEquals(VALID_ID, result.getId());

    }

    @Test
    void deleteCampaign_ShouldDeleteCampaign() {

        when(campaignRepository.findByIdAndUserId(campaign.getId(), user.getId())).thenReturn(Optional.of(campaign));

        campaignService.delete(campaign.getId());
        verify(campaignRepository).delete(campaign);

    }

    @Test
    void deleteCampaign_ShouldThrowCampaignNotFound() {

        when(campaignRepository.findByIdAndUserId(INVALID_ID, user.getId())).thenReturn(Optional.empty());
        assertThrows(CampaignNotFoundException.class, () -> campaignService.delete(INVALID_ID));

    }

    @Test
    void getCampaignStats_ShouldReturnCorrectStats() {

        when(campaignRepository.findByIdAndUserId(campaign.getId(), user.getId())).thenReturn(Optional.of(campaign));

        var email1 = createEmailMessageWithStatus(Status.SENT);
        var email2 = createEmailMessageWithStatus(Status.REPLIED);
        var email3 = createEmailMessageWithStatus(Status.FAILED);
        var email4 = createEmailMessageWithStatus(Status.PENDING);
        var email5 = createEmailMessageWithStatus(Status.PENDING);

        var emails = List.of(email1, email2, email3, email4, email5);

        when(emailMessageRepository.findAllByCampaign(campaign)).thenReturn(emails);

        var result = campaignService.getCampaignStats(VALID_ID);

        assertEquals(5, result.total());
        assertEquals(2, result.sent()); // 1 sent + 1 replied
        assertEquals(1, result.failed());
        assertEquals(1, result.replied());
        assertEquals(2, result.pending());


        verify(campaignRepository).findByIdAndUserId(VALID_ID, user.getId());
        verify(emailMessageRepository).findAllByCampaign(campaign);



    }

    @Test
    void getCampaignStats_ShouldThrowException_WhenCampaignNotFound() {

        when(campaignRepository.findByIdAndUserId(INVALID_ID, user.getId()))
                .thenReturn(Optional.empty());

        assertThrows(CampaignNotFoundException.class,
                () -> campaignService.getCampaignStats(INVALID_ID));
    }


    EmailMessage createEmailMessageWithStatus(Status status){
        EmailMessage emailMessage = new EmailMessage();
        emailMessage.setStatus(status);
        return emailMessage;
    }

}
