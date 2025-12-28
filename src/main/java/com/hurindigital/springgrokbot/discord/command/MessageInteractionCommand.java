package com.hurindigital.springgrokbot.discord.command;

import discord4j.core.event.domain.interaction.MessageInteractionEvent;

public interface MessageInteractionCommand extends Command<MessageInteractionEvent> {

    @Override
    default Class<? extends MessageInteractionEvent> getType() {
        return MessageInteractionEvent.class;
    }

}
