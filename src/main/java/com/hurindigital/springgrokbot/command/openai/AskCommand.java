package com.hurindigital.springgrokbot.command.openai;

import com.hurindigital.springgrokbot.command.Command;
import com.hurindigital.springgrokbot.service.ChatService;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.command.ApplicationCommandInteractionOptionValue;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.ThreadChannel;
import discord4j.core.spec.InteractionReplyEditSpec;
import discord4j.core.spec.StartThreadFromMessageSpec;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.util.function.Function;

@Slf4j
public class AskCommand implements Command {

    public static final String QUERY_OPTION = "query";

    private final ChatService chatService;

    public AskCommand(ChatService chatService) {
        this.chatService = chatService;
    }

    @Override
    public String getName() {
        return "ask";
    }

    @Override
    public Mono<Void> handle(ChatInputInteractionEvent event) {
        final String query = getQuery(event);
        return event.deferReply()
                .then(event.createFollowup("Let's continue this conversation in the thread below."))
                .flatMap(followUp -> followUp.startThread(StartThreadFromMessageSpec.builder()
                                .name(query)
                        .build()))
                .flatMap(threadChannel -> chatService.ask(query, threadChannel.getId().asString())
                        .flatMap(threadChannel::createMessage))
                .onErrorResume(error -> event.createFollowup("Failed to create thread: " + error.getMessage())
                        .withEphemeral(true))
                .then();
    }

    private String getQuery(ChatInputInteractionEvent event) {
        return event.getOption(QUERY_OPTION)
                .flatMap(ApplicationCommandInteractionOption::getValue)
                .map(ApplicationCommandInteractionOptionValue::asString)
                //TODO: Better error handling
                .orElseThrow();
    }

}
