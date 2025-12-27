package com.hurindigital.springgrokbot.discord;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

public class Chunker {

    private static final int MAX_LENGTH = 2000;

    private static final String CODE_BLOCK = "```";

    private Chunker() {

    }

    public static Flux<String> chunk(Mono<String> raw) {
        return raw.defaultIfEmpty("")
                .flatMapMany(fullText -> {
                    if (fullText.isBlank()) {
                        return Flux.just("I couldn't generate a response.");
                    }

                    List<String> chunks = new ArrayList<>();
                    int start = 0;

                    while (start < fullText.length()) {
                        int end = Math.min(start + MAX_LENGTH, fullText.length());
                        if (end < fullText.length()) {
                            // Prefer newline
                            int lastNewline = fullText.lastIndexOf('\n', end);
                            if (lastNewline > start) {
                                end = lastNewline + 1;
                            } else {
                                // Then space
                                int lastSpace = fullText.lastIndexOf(' ', end);
                                if (lastSpace > start) {
                                    end = lastSpace + 1;
                                }
                                // Else hard split at MAX_LENGTH
                            }
                        }

                        String chunk = fullText.substring(start, end).stripTrailing();
                        if (!chunk.isBlank()) {
                            chunks.add(chunk);
                        }

                        start = end;
                    }

                    return Flux.fromIterable(chunks);
                });
    }

}
