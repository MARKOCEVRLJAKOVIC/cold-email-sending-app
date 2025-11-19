package dev.marko.EmailSender.email.reply;


import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.gmail.Gmail;
import dev.marko.EmailSender.email.connection.gmailOAuth.GoogleOAuth2Properties;
import dev.marko.EmailSender.security.AesEncryptor;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.GeneralSecurityException;

@AllArgsConstructor
@Component
public class GmailServiceFactory {

    private final AesEncryptor aesEncryptor;
    private final GoogleOAuth2Properties properties;

    public Gmail createService(String accessToken, String refreshToken) throws IOException, GeneralSecurityException {

        GoogleCredential credential = new GoogleCredential.Builder()
                .setTransport(GoogleNetHttpTransport.newTrustedTransport())
                .setJsonFactory(JacksonFactory.getDefaultInstance())
                .setClientSecrets(properties.getClientId(), properties.getClientSecret())
                .build()
                .setAccessToken(accessToken)
                .setRefreshToken(refreshToken);


        return new Gmail.Builder(
                com.google.api.client.googleapis.javanet.GoogleNetHttpTransport.newTrustedTransport(),
                com.google.api.client.json.jackson2.JacksonFactory.getDefaultInstance(),
                credential)
                .setApplicationName("EmailSender")
                .build();
    }
}

