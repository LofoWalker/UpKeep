package com.upkeep.application.usecase;

import com.upkeep.application.port.in.pkg.ImportPackagesFromListUseCase.ImportFromListCommand;
import com.upkeep.application.port.in.pkg.ImportPackagesFromListUseCase.ImportListResult;
import com.upkeep.application.port.out.membership.MembershipRepository;
import com.upkeep.application.port.out.pkg.PackageRepository;
import com.upkeep.domain.exception.MembershipNotFoundException;
import com.upkeep.domain.model.company.CompanyId;
import com.upkeep.domain.model.customer.CustomerId;
import com.upkeep.domain.model.membership.Membership;
import com.upkeep.domain.model.membership.MembershipId;
import com.upkeep.domain.model.membership.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ImportPackagesFromListUseCaseImplTest {

    private PackageRepository packageRepository;
    private MembershipRepository membershipRepository;
    private ImportPackagesFromListUseCaseImpl useCase;

    private static final String CUSTOMER_ID = UUID.randomUUID().toString();
    private static final String COMPANY_ID = UUID.randomUUID().toString();

    @BeforeEach
    void setUp() {
        packageRepository = mock(PackageRepository.class);
        membershipRepository = mock(MembershipRepository.class);
        useCase = new ImportPackagesFromListUseCaseImpl(packageRepository, membershipRepository);
    }

    @Test
    void shouldImportValidPackages() {
        Membership membership = createTestMembership();
        when(membershipRepository.findByCustomerIdAndCompanyId(any(CustomerId.class), any(CompanyId.class)))
                .thenReturn(Optional.of(membership));
        when(packageRepository.findExistingNamesByCompanyId(any(CompanyId.class), anySet()))
                .thenReturn(Set.of());

        ImportFromListCommand command = new ImportFromListCommand(
                COMPANY_ID, CUSTOMER_ID, List.of("lodash", "express", "react"));

        ImportListResult result = useCase.execute(command);

        assertEquals(3, result.importedCount());
        assertEquals(0, result.skippedCount());
        assertEquals(0, result.invalidCount());
    }

    @Test
    void shouldSkipDuplicatesAndReportInvalid() {
        Membership membership = createTestMembership();
        when(membershipRepository.findByCustomerIdAndCompanyId(any(CustomerId.class), any(CompanyId.class)))
                .thenReturn(Optional.of(membership));
        when(packageRepository.findExistingNamesByCompanyId(any(CompanyId.class), anySet()))
                .thenReturn(Set.of("lodash"));

        ImportFromListCommand command = new ImportFromListCommand(
                COMPANY_ID, CUSTOMER_ID, List.of("lodash", "express", "INVALID NAME!!", "@types/node"));

        ImportListResult result = useCase.execute(command);

        assertEquals(2, result.importedCount());
        assertEquals(1, result.skippedCount());
        assertEquals(1, result.invalidCount());
        assertTrue(result.skippedNames().contains("lodash"));
        assertTrue(result.invalidNames().contains("INVALID NAME!!"));
        assertTrue(result.importedNames().contains("express"));
        assertTrue(result.importedNames().contains("@types/node"));
    }

    @Test
    void shouldThrowWhenUserNotMember() {
        when(membershipRepository.findByCustomerIdAndCompanyId(any(CustomerId.class), any(CompanyId.class)))
                .thenReturn(Optional.empty());

        ImportFromListCommand command = new ImportFromListCommand(
                COMPANY_ID, CUSTOMER_ID, List.of("lodash"));

        assertThrows(MembershipNotFoundException.class, () -> useCase.execute(command));
    }

    @Test
    void shouldSkipEmptyAndBlankNames() {
        Membership membership = createTestMembership();
        when(membershipRepository.findByCustomerIdAndCompanyId(any(CustomerId.class), any(CompanyId.class)))
                .thenReturn(Optional.of(membership));
        when(packageRepository.findExistingNamesByCompanyId(any(CompanyId.class), anySet()))
                .thenReturn(Set.of());

        ImportFromListCommand command = new ImportFromListCommand(
                COMPANY_ID, CUSTOMER_ID, List.of("lodash", "", "  ", "react"));

        ImportListResult result = useCase.execute(command);

        assertEquals(2, result.importedCount());
        assertEquals(0, result.invalidCount());
    }

    private Membership createTestMembership() {
        return Membership.reconstitute(
                MembershipId.from(UUID.randomUUID()),
                CustomerId.from(CUSTOMER_ID),
                CompanyId.from(COMPANY_ID),
                Role.MEMBER,
                Instant.now(),
                Instant.now()
        );
    }
}

