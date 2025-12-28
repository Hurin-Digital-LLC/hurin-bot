package com.hurindigital.springgrokbot.discord;

import com.hurindigital.springgrokbot.config.DiscordProperties;
import com.hurindigital.springgrokbot.domain.Thread;
import com.hurindigital.springgrokbot.service.ThreadTrackerService;
import discord4j.common.util.Snowflake;
import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.channel.ThreadChannel;
import discord4j.core.spec.ThreadChannelEditSpec;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;

@Slf4j
public class ThreadAutoCloser implements ApplicationRunner {

    private final DiscordProperties properties;

    private final DiscordClient discordClient;

    private final ThreadTrackerService threadTrackerService;

    private final ScheduledExecutorService scheduler = ThreadAutoCloseThreadFactory.scheduledExecutorService();

    public ThreadAutoCloser(DiscordProperties properties, DiscordClient discordClient, ThreadTrackerService threadTrackerService) {
        this.properties = properties;
        this.discordClient = discordClient;
        this.threadTrackerService = threadTrackerService;
    }

    @Override
    public void run(ApplicationArguments args) {
        discordClient.gateway()
                .login()
                .doOnSuccess(scheduleAutoCloser()
                        .andThen(_ -> log.info("Thread Auto Close Scheduler started.")))
                .flatMap(GatewayDiscordClient::onDisconnect)
                .doOnError(error -> log.error("ThreadAutoCloser gateway error", error))
                .subscribe();
    }

    private Consumer<GatewayDiscordClient> scheduleAutoCloser() {
        log.info("Scheduling thread auto-closer to run every {} seconds", getAutoCloseDuration().getSeconds());
        return gatewayDiscordClient -> scheduler.scheduleAtFixedRate(checkAndCloseInactiveThreads(gatewayDiscordClient),
                getCheckInterval(), getCheckInterval(), TimeUnit.SECONDS);
    }

    private Runnable checkAndCloseInactiveThreads(GatewayDiscordClient client) {
        return () -> {
            log.info("ThreadAutoCloser checking for inactive threads...");
            threadTrackerService.findAllActiveThreads()
                    .flatMap(thread -> client.getChannelById(Snowflake.of(thread.getThreadId()))
                            .ofType(ThreadChannel.class)
                            .filterWhen(isInactive(client))
                            .flatMap(archiveThread())
                            .then(closeThread(thread))
                            .onErrorResume(error -> {
                                log.error("Failed to process thread {}: {}", thread.getThreadId(), error.getMessage());
                                return Mono.empty();
                            }))
                    .subscribeOn(Schedulers.boundedElastic())
                    .subscribe(null, error -> log.error("Error in auto-close task", error),
                            () -> log.info("Inactive thread check completed"));
        };
    }

    private Function<ThreadChannel, Mono<Boolean>> isInactive(GatewayDiscordClient client) {
        return thread -> thread.getLastMessageId()
                .map(messageId -> client.getMessageById(thread.getId(), messageId)
                        .map(message -> Duration.between(message.getTimestamp(), Instant.now())
                                .compareTo(getAutoCloseDuration()) > 0))
                .orElse(Mono.just(true));
    }

    private Function<ThreadChannel, Mono<Void>> archiveThread() {
        return thread -> thread.createMessage("This thread has been inactive for " + getAutoCloseDuration().toMinutes() + " minutes. Archiving...")
                .then(thread.edit(ThreadChannelEditSpec.builder()
                                .archived(true)
                                .locked(true)
                        .build()))
                .doOnSuccess(thread1 -> log.info("Auto-archived thread: {}", thread1.getId().asString()))
                .then();
    }

    private Mono<Void> closeThread(Thread thread) {
        log.info("ThreadAutoCloser closing thread {}", thread.getId());
        return threadTrackerService.close(thread)
                .then();
    }

    private long getCheckInterval() {
        return properties.getThreadAutoCloseInterval().getSeconds();
    }

    private Duration getAutoCloseDuration() {
        return properties.getThreadAutoCloseDuration();
    }

    public void shutdown() {
        if (!scheduler.isShutdown()) {
            scheduler.shutdownNow();
            log.info("ThreadAutoCloser shutting down.");
        }
    }

}
