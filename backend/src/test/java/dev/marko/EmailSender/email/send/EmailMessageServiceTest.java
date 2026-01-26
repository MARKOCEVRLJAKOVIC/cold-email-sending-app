package dev.marko.EmailSender.email.send;

import dev.marko.EmailSender.dtos.EmailMessageDto;
import dev.marko.EmailSender.email.send.batch.SendBatchEmailsService;
import dev.marko.EmailSender.entities.Campaign;
import dev.marko.EmailSender.entities.EmailMessage;
import dev.marko.EmailSender.entities.Status;
import dev.marko.EmailSender.entities.User;
import dev.marko.EmailSender.mappers.EmailMessageMapper;
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
public class EmailMessageServiceTest {

    @Mock private SendBatchEmailsService sendBatchEmailsService;
    @Mock private EmailMessageRepository emailMessageRepository;
    @Mock private CurrentUserProvider currentUserProvider;
    @Mock private EmailMessageMapper emailMessageMapper;

    @InjectMocks EmailMessageService emailMessageService;

    User user;
    EmailMessage emailMessage;
    EmailMessageDto emailMessageDto;
    Campaign campaign;

    Long VALID_ID = 1L;
    Long INVALID_ID = 99L;

    @BeforeEach
    void setup(){

        user = new User();
        user.setId(VALID_ID);

        campaign = new Campaign();
        campaign.setId(VALID_ID);

        emailMessage = new EmailMessage();
        emailMessage.setId(VALID_ID);
        emailMessage.setCampaign(campaign);

        emailMessageDto = new EmailMessageDto();
        emailMessageDto.setId(VALID_ID);

        when(currentUserProvider.getCurrentUser()).thenReturn(user);

    }

    @Test
    void findAllMessagesFromUser_ShouldReturnListOfEmailMessageDtos(){

        emailMessage.setStatus(Status.SENT);
        when(emailMessageRepository
                .findAllByUserIdAndStatusIn(user.getId(), List.of(Status.SENT, Status.REPLIED)))
                .thenReturn(List.of(emailMessage));
        when(emailMessageMapper.toListDto(List.of(emailMessage))).thenReturn(List.of(emailMessageDto));

        var result = emailMessageService.findAllMessagesFromUser();

        assertEquals(1, result.size());
        assertEquals(VALID_ID, result.get(0).getId());
        verify(emailMessageRepository).findAllByUserIdAndStatusIn(user.getId(), List.of(Status.SENT, Status.REPLIED));

    }

    @Test
    void findAllMessagesFromCampaign_ShouldReturnListOfEmailMessageDtos(){

        when(emailMessageRepository
                .findAllByCampaignIdAndUserIdAndStatusIn
                        (campaign.getId(), user.getId(), List.of(Status.SENT, Status.REPLIED)))
                .thenReturn(List.of(emailMessage));
        when(emailMessageMapper.toListDto(List.of(emailMessage))).thenReturn(List.of(emailMessageDto));

        var result = emailMessageService.findAllMessagesFromCampaign(campaign.getId());

        assertEquals(1, result.size());
        assertEquals(VALID_ID, result.get(0).getId());
        verify(emailMessageRepository).findAllByCampaignIdAndUserIdAndStatusIn
                (campaign.getId(), user.getId(), List.of(Status.SENT, Status.REPLIED));

    }


    @Test
    void getEmailMessage_ShouldReturnEmailMessageDto(){
        when(emailMessageRepository.findByIdAndUserId(emailMessage.getId(), user.getId()))
                .thenReturn(Optional.of(emailMessage));
        when(emailMessageMapper.toDto(emailMessage)).thenReturn(emailMessageDto);

        var result = emailMessageService.getEmailMessage(emailMessage.getId());

        assertEquals(VALID_ID, result.getId());
        verify(emailMessageRepository).findByIdAndUserId(emailMessage.getId(), user.getId());

    }

    @Test
    void getEmailMessage_ShouldThrowEmailNotFoundException() {

        // throw exception by providing non-existing id
        when(emailMessageRepository.findByIdAndUserId(INVALID_ID, user.getId())).thenReturn(Optional.empty());
        assertThrows(EmailMessageNotFoundException.class, () -> emailMessageService.getEmailMessage(INVALID_ID));

    }

    @Test
    void updateEmailMessages_ShouldUpdateEmailMessagesAndReturnDto(){

        UpdateEmailMessageRequest request = new UpdateEmailMessageRequest();

        when(emailMessageRepository.findByIdAndUserId(emailMessage.getId(), user.getId()))
                .thenReturn(Optional.of(emailMessage));
        when(emailMessageMapper.toDto(emailMessage)).thenReturn(emailMessageDto);

        var result = emailMessageService.updateEmailMessage(emailMessage.getId(), request);

        assertEquals(VALID_ID, result.getId());
        verify(emailMessageMapper).update(request, emailMessage);
        verify(emailMessageRepository).save(emailMessage);

    }

    @Test
    void updateEmailMessages_ShouldThrowIllegalStateExceptionForIncorrectEmailMessageStatus(){
        UpdateEmailMessageRequest request = new UpdateEmailMessageRequest();


        // setting incorrect status, API expects Status.PENDING
        emailMessage.setStatus(Status.SENT);
        when(emailMessageRepository.findByIdAndUserId(emailMessage.getId(), user.getId()))
                .thenReturn(Optional.of(emailMessage));
        assertThrows(IllegalStateException.class, () ->
                emailMessageService.updateEmailMessage(emailMessage.getId(), request));


    }

    @Test
    void deleteEmailMessage_ShouldDeleteEmailMessage(){

        when(emailMessageRepository.findByIdAndUserId(emailMessage.getId(), user.getId()))
                .thenReturn(Optional.of(emailMessage));

        emailMessageService.deleteEmailMessage(emailMessage.getId());

        verify(emailMessageRepository).delete(emailMessage);

    }

    @Test
    void deleteEmailMessage_ShouldThrowEmailMessageNotFindException(){

        when(emailMessageRepository.findByIdAndUserId(INVALID_ID, user.getId()))
                .thenReturn(Optional.empty());
        assertThrows(EmailMessageNotFoundException.class, () -> emailMessageService.deleteEmailMessage(INVALID_ID));
    }

}