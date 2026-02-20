package com.upkeep.infrastructure.adapter.in.rest.pkg;

import java.util.List;

public record PackageListResponse(
        List<PackageItemResponse> packages,
        long totalCount,
        int page,
        int size
) {
    public record PackageItemResponse(
            String id,
            String name,
            String registry,
            String importedAt
    ) {
    }
}

