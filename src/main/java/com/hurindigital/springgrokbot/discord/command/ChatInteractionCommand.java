package com.hurindigital.springgrokbot.discord.command;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;

public interface ChatInteractionCommand extends Command<ChatInputInteractionEvent> {

    @Override
    default Class<? extends ChatInputInteractionEvent> getType() {
        return ChatInputInteractionEvent.class;
    }

}
