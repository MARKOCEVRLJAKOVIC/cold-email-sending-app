package dev.marko.EmailSender.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
public class AsyncConfig {

    @Bean(name = "emailTaskExecutor")
    public Executor emailTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // threads always active
        executor.setCorePoolSize(10);
        // max pool size
        executor.setMaxPoolSize(20);
        // queue capacity
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("EmailSender-");
        executor.initialize();
        return executor;
    }
}