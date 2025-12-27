package com.hurindigital.springgrokbot.domain;

import java.time.Instant;
import java.util.UUID;

public interface Thread {

    UUID getId();

    long getThreadId();

    Instant getCreated();

    Instant getClosed();

}
