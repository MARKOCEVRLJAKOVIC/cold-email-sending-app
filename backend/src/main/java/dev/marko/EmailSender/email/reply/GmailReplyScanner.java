package dev.marko.EmailSender.email.reply;

import dev.marko.EmailSender.entities.SmtpCredentials;
import dev.marko.EmailSender.entities.SmtpType;
import dev.marko.EmailSender.repositories.SmtpRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GmailReplyScanner implements EmailReplyScanner{

    private final SmtpRepository smtpRepository;
    private final GmailMessageFetcher gmailMessageFetcher;
    private final EmailReplyProcessor emailReplyProcessor;

    @Override
    public SmtpType supports() {
        return SmtpType.GMAIL;
    }

    @Override
    public void checkReplies() {

        var credsList = smtpRepository.findAllBySmtpTypeAndEnabled(SmtpType.GMAIL, true);

        for (SmtpCredentials credentials : credsList) {

            var messages = gmailMessageFetcher.fetchUnreadMessages(credentials);

            emailReplyProcessor.processReplies(messages, credentials);
        }

    }


}