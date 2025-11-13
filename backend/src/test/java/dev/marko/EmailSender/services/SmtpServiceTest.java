package dev.marko.EmailSender.services;

import dev.marko.EmailSender.dtos.SmtpDto;
import dev.marko.EmailSender.entities.SmtpCredentials;
import dev.marko.EmailSender.entities.User;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
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


    @BeforeEach
    void setup(){

        user = new User();
        user.setId(1L);

        smtp = new SmtpCredentials();
        smtp.setId(1L);
        smtp.setUser(user);

        smtpDto = new SmtpDto();
        smtpDto.setId(1L);

        when(currentUserProvider.getCurrentUser()).thenReturn(user);

    }

    @Test
    void getAllSmtpCredentials_ShouldReturnListOfDtos(){

        when(smtpRepository.findAllByUserId(1L)).thenReturn(List.of(smtp));
        when(smtpMapper.smtpListToDtoList(List.of(smtp))).thenReturn(List.of(smtpDto));

        var result = smtpService.getAllSmtpCredentials();

        assertEquals(1, result.size());
        assertEquals(1L, result.getFirst().getId());
        verify(smtpRepository).findAllByUserId(1L);
    }

}
