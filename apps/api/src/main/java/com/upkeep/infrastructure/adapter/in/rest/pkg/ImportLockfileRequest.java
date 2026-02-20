package com.upkeep.infrastructure.adapter.in.rest.pkg;

public record ImportLockfileRequest(
        String fileContent,
        String filename
) {
}

