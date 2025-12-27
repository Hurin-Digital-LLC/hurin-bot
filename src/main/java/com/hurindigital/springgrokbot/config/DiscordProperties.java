package com.hurindigital.springgrokbot.config;

import lombok.Data;

import java.time.Duration;

@Data
public class DiscordProperties {

    private String token;

    private Long applicationId;

    private Long guildId;

    private Duration threadAutoCloseDuration;

    private Duration threadAutoCloseInterval;

}
