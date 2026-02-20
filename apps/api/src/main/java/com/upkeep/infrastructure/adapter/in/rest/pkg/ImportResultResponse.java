package com.upkeep.infrastructure.adapter.in.rest.pkg;

import java.util.List;

public record ImportResultResponse(
        int importedCount,
        int skippedCount,
        int totalParsed,
        List<String> importedNames,
        List<String> skippedNames
) {
}

