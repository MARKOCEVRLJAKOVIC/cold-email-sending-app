package dev.marko.EmailSender.email.reply;

import com.google.api.services.gmail.model.Message;
import dev.marko.EmailSender.entities.EmailReply;
import dev.marko.EmailSender.entities.SmtpCredentials;
import dev.marko.EmailSender.repositories.EmailMessageRepository;
import dev.marko.EmailSender.repositories.EmailReplyRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@AllArgsConstructor
@Service
public class EmailReplyProcessor {

    private final EmailMessageRepository emailMessageRepository;
    private final ReplyPersistenceService replyPersistenceService;

    public void processReplies(List<Message> messages, SmtpCredentials creds) {

        for (Message msg : messages) {

            String inReplyTo = GmailUtils.getHeader(msg, "In-Reply-To");
            if (inReplyTo == null) continue;

            emailMessageRepository.findByMessageId(inReplyTo)
                        .ifPresent(original -> replyPersistenceService.saveReply(original, msg, creds));
        }
    }

}
