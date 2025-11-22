package dev.marko.EmailSender.email.reply;

import com.google.api.services.gmail.model.Message;
import dev.marko.EmailSender.entities.EmailMessage;
import dev.marko.EmailSender.entities.EmailReply;
import dev.marko.EmailSender.entities.SmtpCredentials;
import dev.marko.EmailSender.entities.Status;
import dev.marko.EmailSender.repositories.EmailMessageRepository;
import dev.marko.EmailSender.repositories.EmailReplyRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
public class ReplyPersistenceService {

    private final EmailReplyRepository replyRepository;
    private final EmailMessageRepository emailMessageRepository;

    public void saveReply(EmailMessage original, Message msg, SmtpCredentials creds) {

        original.setStatus(Status.REPLIED);
        emailMessageRepository.save(original);

        EmailReply reply = EmailReply.builder()
                .originalMessageId(GmailUtils.getHeader(msg, "In-Reply-To"))
                .repliedMessageId(GmailUtils.getHeader(msg, "Message-ID"))
                .senderEmail(GmailUtils.getHeader(msg, "From"))
                .subject(GmailUtils.getHeader(msg, "Subject"))
                .content(msg.getSnippet())
                .receivedAt(java.time.LocalDateTime.now())
                .emailMessage(original)
                .user(creds.getUser())
                .build();

        replyRepository.save(reply);
    }

}
