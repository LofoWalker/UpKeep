package com.upkeep.application.port.in.pkg;

import java.util.List;

public interface ImportPackagesFromLockfileUseCase {

    ImportResult execute(ImportFromLockfileCommand command);

    record ImportFromLockfileCommand(
            String companyId,
            String customerId,
            String fileContent,
            String filename
    ) {}

    record ImportResult(
            int importedCount,
            int skippedCount,
            int totalParsed,
            List<String> importedNames,
            List<String> skippedNames
    ) {}
}

