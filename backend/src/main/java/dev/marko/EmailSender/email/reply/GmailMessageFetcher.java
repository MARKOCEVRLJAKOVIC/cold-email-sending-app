package dev.marko.EmailSender.email.reply;

import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;
import dev.marko.EmailSender.email.connection.OAuthRefreshable;
import dev.marko.EmailSender.entities.SmtpCredentials;
import dev.marko.EmailSender.security.EncryptionService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@AllArgsConstructor
@Service
public class GmailMessageFetcher {

    private final GmailServiceFactory gmailServiceFactory;
    private final EncryptionService encryptionService;
    private final OAuthRefreshable refreshable;

    public List<Message> fetchUnreadMessages(SmtpCredentials creds){
        try {
            creds = refreshable.refreshTokenIfNeeded(creds);

            String accessToken = encryptionService.decryptIfNeeded(creds.getOauthAccessToken());
            String refreshToken = encryptionService.decryptIfNeeded(creds.getOauthRefreshToken());

            Gmail service = gmailServiceFactory.createService(accessToken, refreshToken);

            ListMessagesResponse response = service.users()
                    .messages()
                    .list("me")
                    .setQ("in:inbox is:unread newer_than:1d")
                    .execute();

            if (response.getMessages() == null) return List.of();

            return response
                    .getMessages()
                    .stream()
                    .map(m -> getFullMessage(service, m.getId()))
                    .toList();

        } catch (Exception e) {
            throw new IllegalStateException("Failed Gmail fetch for " + creds.getEmail(), e);
        }
    }

    private Message getFullMessage(Gmail service, String id) {
        try {
            return service.users().messages().get("me", id).setFormat("full").execute();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
