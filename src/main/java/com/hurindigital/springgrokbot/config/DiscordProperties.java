package com.hurindigital.springgrokbot.config;

import lombok.Data;

@Data
public class DiscordProperties {

    private String token;

    private Long applicationId;

    private Long guildId;

}
