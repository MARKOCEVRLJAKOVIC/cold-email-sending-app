package dev.marko.EmailSender.email.schedulesrs;

import dev.marko.EmailSender.entities.EmailMessage;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailSchedulingService {

    private final EmailSendService sendService;
    private final ScheduledExecutorService scheduler;
    private final RedisEmailScheduler redisEmailScheduler;

    @Getter
    @Value("${email.scheduling.default-delay-seconds}")
    private int defaultDelay;

    public void scheduleSingle(EmailMessage message, long delayInSeconds, LocalDateTime scheduledAt) {
//        scheduler.schedule(() -> sendService.sendAndPersist(message),
//                delayInSeconds, TimeUnit.SECONDS);

        String zone = message.getCampaign().getTimezone();

        redisEmailScheduler.scheduleAt(message.getId(), scheduledAt, delayInSeconds, zone);

    }

    public void scheduleBatch(List<EmailMessage> messages, long intervalInSeconds) {
        for (int i = 0; i < messages.size(); i++) {
            EmailMessage message = messages.get(i);
            long delay = i * intervalInSeconds;

//            scheduler.schedule(() -> sendService.sendAndPersist(message),
//                    delay, TimeUnit.SECONDS);

            redisEmailScheduler.schedule(message.getId(), delay);

        }
    }

    public void scheduleFollowUp(EmailMessage message, long delayInSeconds) {
//        scheduler.schedule(() -> sendService.sendAndPersist(message),
//                delayInSeconds, TimeUnit.SECONDS);

        redisEmailScheduler.schedule(message.getId(), delayInSeconds);

    }

}