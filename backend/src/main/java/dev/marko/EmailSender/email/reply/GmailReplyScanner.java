package dev.marko.EmailSender.email.reply;

import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;
import dev.marko.EmailSender.email.connection.EmailConnectionService;
import dev.marko.EmailSender.email.connection.OAuthRefreshable;
import dev.marko.EmailSender.entities.*;
import dev.marko.EmailSender.repositories.EmailMessageRepository;
import dev.marko.EmailSender.repositories.EmailReplyRepository;
import dev.marko.EmailSender.repositories.SmtpRepository;
import dev.marko.EmailSender.security.TokenEncryptor;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GmailReplyScanner {

    private final SmtpRepository smtpRepository;
    private final EmailMessageRepository messageRepo;
    private final EmailReplyRepository replyRepo;
    private final GmailServiceFactory gmailServiceFactory;
    private final TokenEncryptor tokenEncryptor;
    private final OAuthRefreshable refreshable;

    @Scheduled(fixedRate = 300_000) // every 5 minutes
    public void checkReplies() throws IOException, GeneralSecurityException {
        System.out.println("checkReplies invoked at " + java.time.LocalDateTime.now());


        List<SmtpCredentials> credsList = smtpRepository.findAllBySmtpTypeAndEnabled(SmtpType.OAUTH2, true);

        for (SmtpCredentials creds : credsList) {
            try {

                creds = refreshable.refreshTokenIfNeeded(creds);

                String accessToken = tokenEncryptor.decryptIfNeeded(creds.getOauthAccessToken());
                String refreshToken = tokenEncryptor.decryptIfNeeded(creds.getOauthRefreshToken());

                Gmail service = gmailServiceFactory.createService(accessToken, refreshToken);

                ListMessagesResponse response = service.users().messages().list("me")
                        .setQ("in:inbox is:unread newer_than:1d")
                        .execute();

                if (response.getMessages() == null) continue;

                for (Message msgMeta : response.getMessages()) {
                    Message msg = service.users().messages().get("me", msgMeta.getId()).setFormat("full").execute();

                    System.out.println("Found " + (response.getMessages() == null ? 0 : response.getMessages().size()) + " unread messages");

                    String inReplyTo = GmailUtils.getHeader(msg, "In-Reply-To");
                    String messageId = GmailUtils.getHeader(msg, "Message-ID");
                    String from = GmailUtils.getHeader(msg, "From");
                    String subject = GmailUtils.getHeader(msg, "Subject");
                    String snippet = msg.getSnippet();

                    Optional<EmailMessage> originalOpt = messageRepo.findByMessageId(inReplyTo);

                    if (originalOpt.isPresent()) {
                        EmailMessage original = originalOpt.get();
                        original.setStatus(Status.REPLIED);
                        messageRepo.save(original);

                        EmailReply reply = EmailReply.builder()
                                .originalMessageId(inReplyTo)
                                .repliedMessageId(messageId)
                                .senderEmail(from)
                                .subject(subject)
                                .content(snippet)
                                .receivedAt(java.time.LocalDateTime.now())
                                .emailMessage(original)
                                .user(creds.getUser())
                                .build();

                        replyRepo.save(reply);
                    }
                }
            } catch (IllegalStateException e) {
                System.err.println("Failed to refresh token for " + creds.getEmail() + ": " + e.getMessage());
            }
        }
    }
}