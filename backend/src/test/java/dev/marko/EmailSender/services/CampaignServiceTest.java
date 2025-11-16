package dev.marko.EmailSender.services;

import dev.marko.EmailSender.dtos.CampaignDto;
import dev.marko.EmailSender.dtos.CreateCampaignRequest;
import dev.marko.EmailSender.entities.Campaign;
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

    @Mock private CampaignRepository campaignRepository;
    @Mock private CampaignMapper campaignMapper;
    @Mock private CurrentUserProvider currentUserProvider;
    @Mock private EmailMessageRepository emailMessageRepository;

    @InjectMocks CampaignService campaignService;

    User user;
    Campaign campaign;
    CampaignDto campaignDto;

    Long VALID_ID = 1L;
    Long INVALID_ID = 99L;

    @BeforeEach
    void setup(){

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
    void getAllCampaignFromUser_ShouldReturnListOfUserDtos(){

        when(campaignRepository.findAllByUserId(user.getId())).thenReturn(List.of(campaign));
        when(campaignMapper.toDto(campaign)).thenReturn(campaignDto);

        var result = campaignService.getAll();

        assertEquals(1, result.size());
        assertEquals(VALID_ID, result.getFirst().getId());
        verify(campaignRepository).findAllByUserId(user.getId());

    }

    @Test
    void getSmtp_ShouldReturnDto(){

        when(campaignRepository.findByIdAndUserId(campaign.getId(), user.getId())).thenReturn(Optional.of(campaign));
        when(campaignMapper.toDto(campaign)).thenReturn(campaignDto);

        var result = campaignService.getById(campaign.getId());

        assertEquals(VALID_ID, result.getId());
        verify(campaignRepository).findByIdAndUserId(campaign.getId(), user.getId());

    }

    @Test
    void getSmtp_ShouldThrowException() {

        // throw exception by providing non-existing id
        when(campaignRepository.findByIdAndUserId(INVALID_ID, user.getId())).thenReturn(Optional.empty());
        assertThrows(CampaignNotFoundException.class, () -> campaignService.getById(INVALID_ID));

    }

    @Test
    void createCampaign_ShouldCreateNewCampaignAndReturnDto(){

        CreateCampaignRequest request = new CreateCampaignRequest();
        when(campaignMapper.toEntity(request)).thenReturn(campaign);
        when(campaignMapper.toDto(campaign)).thenReturn(campaignDto);

        var result = campaignService.create(request);

        verify(campaignRepository).save(campaign);
        assertEquals(VALID_ID, result.getId());

    }

    @Test
    void deleteSmtp_ShouldDeleteSmtp(){

        when(campaignRepository.findByIdAndUserId(campaign.getId(), user.getId())).thenReturn(Optional.of(campaign));

        campaignService.delete(campaign.getId());
        verify(campaignRepository).delete(campaign);

    }

    @Test
    void deleteSmtp_ShouldThrowEmailNotFound(){

        when(campaignRepository.findByIdAndUserId(INVALID_ID, user.getId())).thenReturn(Optional.empty());
        assertThrows(CampaignNotFoundException.class, () -> campaignService.delete(INVALID_ID));

    }

}
