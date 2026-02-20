package com.upkeep.application.port.in.pkg;

import java.util.List;

public interface ListCompanyPackagesUseCase {

    PackageListResult execute(ListPackagesQuery query);

    record ListPackagesQuery(
            String companyId,
            String customerId,
            String search,
            int page,
            int size
    ) {}

    record PackageListResult(
            List<PackageItem> packages,
            long totalCount,
            int page,
            int size
    ) {}

    record PackageItem(
            String id,
            String name,
            String registry,
            String importedAt
    ) {}
}

