package dev.marko.EmailSender.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Configuration
public class SchedulerConfig {

    @Bean
    public ScheduledExecutorService emailScheduler() {
        return Executors.newScheduledThreadPool(5);
    }

}
