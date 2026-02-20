package com.upkeep.infrastructure.adapter.in.rest.pkg;

import java.util.List;

public record ImportListResultResponse(
        int importedCount,
        int skippedCount,
        int invalidCount,
        List<String> importedNames,
        List<String> skippedNames,
        List<String> invalidNames
) {
}

