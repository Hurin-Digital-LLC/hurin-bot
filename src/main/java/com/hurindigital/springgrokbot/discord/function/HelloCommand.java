package com.hurindigital.springgrokbot.discord.function;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.entity.Member;
import reactor.core.publisher.Mono;

public class HelloCommand implements Command {
    @Override
    public String getName() {
        return "hello";
    }

    @Override
    public Mono<Void> handle(ChatInputInteractionEvent event) {
        String displayName = event.getInteraction().getMember()
                .map(Member::getDisplayName)
                .orElse(event.getInteraction().getUser().getUsername());
        return event.reply("Hello, " + displayName + "!");
    }
}
