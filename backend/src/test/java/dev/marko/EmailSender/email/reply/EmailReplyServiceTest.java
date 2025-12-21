package dev.marko.EmailSender.email.reply;

import dev.marko.EmailSender.dtos.EmailMessageDto;
import dev.marko.EmailSender.entities.*;
import dev.marko.EmailSender.exception.EmailNotFoundException;
import dev.marko.EmailSender.mappers.EmailReplyMapper;
import dev.marko.EmailSender.repositories.EmailReplyRepository;
import dev.marko.EmailSender.repositories.SmtpRepository;
import dev.marko.EmailSender.security.CurrentUserProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailReplyServiceTest {

    @Mock
    private CurrentUserProvider currentUserProvider;

    @Mock
    private SmtpRepository smtpRepository;

    @Mock
    private EmailReplyRepository replyRepository;

    @Mock
    private EmailReplyMapper emailReplyMapper;

    @Mock
    private ReplyResponseService replyResponseService;

    @InjectMocks
    private EmailReplyService emailReplyService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);

        when(currentUserProvider.getCurrentUser()).thenReturn(user);
    }

    @Test
    void getAllRepliesFromUser_shouldReturnListOfDtos() {
        // given
        EmailReply reply = new EmailReply();
        List<EmailReply> replies = List.of(reply);

        EmailReplyDto dto = new EmailReplyDto();
        List<EmailReplyDto> dtoList = List.of(dto);

        when(replyRepository.findAllByUserId(user.getId())).thenReturn(replies);
        when(emailReplyMapper.toListDto(replies)).thenReturn(dtoList);

        // when
        List<EmailReplyDto> result = emailReplyService.getAllRepliesFromUser();

        // then
        assertEquals(1, result.size());
        verify(replyRepository).findAllByUserId(user.getId());
        verify(emailReplyMapper).toListDto(replies);
    }

    @Test
    void getEmailReply_shouldReturnDto_whenReplyExists() {
        // given
        Long replyId = 10L;
        EmailReply reply = new EmailReply();
        EmailReplyDto dto = new EmailReplyDto();

        when(replyRepository.findByIdAndUserId(replyId, user.getId()))
                .thenReturn(Optional.of(reply));
        when(emailReplyMapper.toDto(reply)).thenReturn(dto);

        // when
        EmailReplyDto result = emailReplyService.getEmailReply(replyId);

        // then
        assertNotNull(result);
        verify(replyRepository).findByIdAndUserId(replyId, user.getId());
        verify(emailReplyMapper).toDto(reply);
    }

    @Test
    void getEmailReply_shouldThrowException_whenReplyNotFound() {
        // given
        Long replyId = 10L;

        when(replyRepository.findByIdAndUserId(replyId, user.getId()))
                .thenReturn(Optional.empty());

        // then
        assertThrows(EmailReplyNotFoundException.class,
                () -> emailReplyService.getEmailReply(replyId));
    }

    @Test
    void respondToReply_shouldCreateReplyMessage() {
        // given
        Long replyId = 5L;

        EmailReply reply = new EmailReply();
        EmailMessage message = new EmailMessage();
        SmtpCredentials smtp = new SmtpCredentials();

        smtp.setId(3L);
        message.setSmtpCredentials(smtp);
        reply.setEmailMessage(message);

        EmailReplyResponseDto responseDto = new EmailReplyResponseDto();
        EmailMessageDto resultDto = new EmailMessageDto();

        when(replyRepository.findByIdAndUserId(replyId, user.getId()))
                .thenReturn(Optional.of(reply));
        when(smtpRepository.findByIdAndUserId(smtp.getId(), user.getId()))
                .thenReturn(Optional.of(smtp));
        when(replyResponseService.createReplyMessage(
                reply, message, responseDto, smtp, user))
                .thenReturn(resultDto);

        // when
        EmailMessageDto result =
                emailReplyService.respondToReply(replyId, responseDto);

        // then
        assertNotNull(result);
        verify(replyResponseService).createReplyMessage(
                reply, message, responseDto, smtp, user);
    }

    @Test
    void deleteReply_shouldDeleteReply() {
        // given
        Long replyId = 7L;
        EmailReply reply = new EmailReply();

        when(replyRepository.findByIdAndUserId(replyId, user.getId()))
                .thenReturn(Optional.of(reply));

        // when
        emailReplyService.deleteReply(replyId);

        // then
        verify(replyRepository).delete(reply);
    }
}

