package com.hurindigital.springgrokbot.discord.command.ask;

import com.hurindigital.springgrokbot.discord.command.MessageInteractionCommand;
import com.hurindigital.springgrokbot.service.ChatService;
import com.hurindigital.springgrokbot.service.ThreadTrackerService;
import discord4j.core.event.domain.interaction.MessageInteractionEvent;
import discord4j.core.object.entity.Attachment;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.ThreadChannel;
import discord4j.core.spec.StartThreadFromMessageSpec;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.ai.content.Media;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@Slf4j
public class AskHurinCommand implements MessageInteractionCommand {

    private final ThreadTrackerService threadTrackerService;

    private final ChatService chatService;

    public AskHurinCommand(ThreadTrackerService threadTrackerService, ChatService chatService) {
        this.threadTrackerService = threadTrackerService;
        this.chatService = chatService;
    }

    @Override
    public String getName() {
        return "Ask Hurin";
    }

    @Override
    public Mono<Void> handle(MessageInteractionEvent event) {
        return event.deferReply()
                .then(event.getTargetMessage())
                .flatMap(processTargetMessage(event))
                .onErrorResume(error -> {
                    log.error(error.getMessage(), error);
                    return Mono.empty();
                })
                .then();
    }

    private Function<Message, Mono<Void>> processTargetMessage(MessageInteractionEvent event) {
        return message -> extractMedia(message)
                .collectList()
                .flatMap(media -> {
                    final String content = message.getContent();
                    final String prompt = buildPrompt(content, !media.isEmpty());
                    final String title = buildThreadTitle(content);
                    return event.createFollowup("Creating thread to discuss this message...")
                            .flatMap(followUp -> followUp.startThread(StartThreadFromMessageSpec.builder()
                                            .name(title)
                                    .build()))
                            .flatMap(thread -> threadTrackerService.track(thread.getId().asLong())
                                    .thenReturn(thread))
                            .flatMap(thread -> thread.type()
                                    .then(chatService.ask(prompt, thread.getId().asString(), media)
                                            .complete()
                                            .delayElements(Duration.ofMillis(600))
                                            .flatMap(thread::createMessage, 1)
                                            .then())
                                    .then(event.editReply("Thread created! Analysis sent below."))
                                    .then());
                });
    }

    private Flux<Media> extractMedia(Message message) {
        return Flux.fromIterable(message.getAttachments())
                .flatMap(attachment -> {
                    Optional<MimeType> mimeType = attachment.getContentType()
                            .map(MimeTypeUtils::parseMimeType);
                    return mimeType.map(type -> Mono.justOrEmpty(new Media(type, URI.create(attachment.getUrl()))))
                            .orElseGet(Mono::empty);
                });
    }

    private String buildPrompt(String content, boolean hasMedia) {
        if (content.isBlank() && hasMedia) {
            return "Please analyze or describe the attached media.";
        }
        if (content.isBlank()) {
            return "Please analyze this message (it may contain embeds or attachments).";
        }
        if (hasMedia) {
            return """
                    Please analyze the following message and its attached media:
                    
                    "%s"
                    """.formatted(content);
        }
        return """
                Please respond to or analyze the following message:
                
                "%s"
                """.formatted(content);
    }

    private String buildThreadTitle(String content) {
        if (content.isBlank()) {
            return "Ask about this message";
        }
        String trimmed = content.replaceAll("\n", " ").trim();
        return trimmed.length() > 90 ? trimmed.substring(0, 90).trim() + "..." : trimmed;
    }

}
