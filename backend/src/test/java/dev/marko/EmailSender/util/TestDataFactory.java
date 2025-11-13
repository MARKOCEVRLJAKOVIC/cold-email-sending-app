package dev.marko.EmailSender.util;

import dev.marko.EmailSender.dtos.SmtpDto;
import dev.marko.EmailSender.entities.SmtpCredentials;
import dev.marko.EmailSender.entities.User;

public class TestDataFactory {

    public static final Long VALID_ID = 1L;
    public static final Long INVALID_ID = 99L;

    public static User createUser() {
        User user = new User();
        user.setId(VALID_ID);
        return user;
    }

    public static SmtpCredentials createSmtp(User user) {
        SmtpCredentials smtp = new SmtpCredentials();
        smtp.setId(VALID_ID);
        smtp.setUser(user);
        return smtp;
    }

    public static SmtpDto createSmtpDto() {
        SmtpDto dto = new SmtpDto();
        dto.setId(VALID_ID);
        return dto;
    }
}
