package com.hurindigital.springgrokbot.command.openai;

import com.hurindigital.springgrokbot.command.Command;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.ThreadChannel;
import discord4j.core.spec.InteractionReplyEditSpec;
import discord4j.core.spec.StartThreadFromMessageSpec;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.util.function.Function;

@Slf4j
public class AskCommand implements Command {

    @Override
    public String getName() {
        return "ask";
    }

    @Override
    public Mono<Void> handle(ChatInputInteractionEvent event) {
        return event.deferReply()
                .then(event.createFollowup("change me"))
                .flatMap(followUp -> followUp.startThread(StartThreadFromMessageSpec.builder()
                                .name("also change me")
                        .build()))
                .flatMap(thread -> thread.createMessage("Welcome! Let's continue the conversation here."))
                .onErrorResume(error -> event.createFollowup("Failed to create thread: " + error.getMessage())
                        .withEphemeral(true))
                .then();
    }

}
