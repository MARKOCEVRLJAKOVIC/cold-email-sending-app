package dev.marko.EmailSender.services;

import dev.marko.EmailSender.dtos.SmtpDto;
import dev.marko.EmailSender.entities.SmtpCredentials;
import dev.marko.EmailSender.entities.User;
import dev.marko.EmailSender.exception.EmailNotFoundException;
import dev.marko.EmailSender.mappers.SmtpMapper;
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

import static dev.marko.EmailSender.util.TestDataFactory.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SmtpServiceTest {

    @Mock private SmtpRepository smtpRepository;
    @Mock private SmtpMapper smtpMapper;
    @Mock private CurrentUserProvider currentUserProvider;

    @InjectMocks SmtpService smtpService;

    User user;
    SmtpCredentials smtp;
    SmtpDto smtpDto;

    Long VALID_ID = 1L;
    Long INVALID_ID = 99L;

    @BeforeEach
    void setup() {

        // create entities with valid/invalid credentials
        user = createUser();
        smtp = createSmtp(user);
        smtpDto = createSmtpDto();

        when(currentUserProvider.getCurrentUser()).thenReturn(user);
    }

    @Test
    void getAllSmtpCredentials_ShouldReturnListOfDtos(){

        when(smtpRepository.findAllByUserId(1L)).thenReturn(List.of(smtp));
        when(smtpMapper.smtpListToDtoList(List.of(smtp))).thenReturn(List.of(smtpDto));

        var result = smtpService.getAllSmtpCredentials();

        assertEquals(1, result.size());
        assertEquals(VALID_ID, result.getFirst().getId());
        verify(smtpRepository).findAllByUserId(user.getId());
    }

    @Test
    void getSmtp_ShouldReturnDto(){

        when(smtpRepository.findByIdAndUserId(smtp.getId(), user.getId())).thenReturn(Optional.of(smtp));
        when(smtpMapper.toDto(smtp)).thenReturn(smtpDto);

        var result = smtpService.getEmail(smtp.getId());

        assertEquals(VALID_ID, result.getId());
        verify(smtpRepository).findByIdAndUserId(smtp.getId(), user.getId());

    }

    @Test
    void getSmtp_ShouldThrowException() {

        // throw exception by providing non-existing id
        when(smtpRepository.findByIdAndUserId(INVALID_ID, user.getId())).thenReturn(Optional.empty());
        assertThrows(EmailNotFoundException.class, () -> smtpService.getEmail(INVALID_ID));


    }



}
