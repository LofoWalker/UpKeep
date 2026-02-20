package com.upkeep.application.port.in.pkg;

import java.util.List;

public interface ImportPackagesFromListUseCase {

    ImportListResult execute(ImportFromListCommand command);

    record ImportFromListCommand(
            String companyId,
            String customerId,
            List<String> packageNames
    ) {}

    record ImportListResult(
            int importedCount,
            int skippedCount,
            int invalidCount,
            List<String> importedNames,
            List<String> skippedNames,
            List<String> invalidNames
    ) {}
}

