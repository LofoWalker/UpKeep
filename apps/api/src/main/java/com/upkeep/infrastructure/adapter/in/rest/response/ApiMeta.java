package com.upkeep.infrastructure.adapter.in.rest.response;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.UUID;
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiMeta(
    Instant timestamp,
    String traceId,
    Integer page,
    Integer pageSize,
    Long totalItems
) {
    public static ApiMeta now() {
        return new ApiMeta(
            Instant.now(),
            UUID.randomUUID().toString(),
            null, null, null
        );
    }
}
