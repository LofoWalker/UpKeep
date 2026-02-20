package com.upkeep.application.usecase;

import com.upkeep.application.port.in.pkg.ImportPackagesFromLockfileUseCase.ImportFromLockfileCommand;
import com.upkeep.application.port.in.pkg.ImportPackagesFromLockfileUseCase.ImportResult;
import com.upkeep.application.port.out.membership.MembershipRepository;
import com.upkeep.application.port.out.pkg.LockfileParser;
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
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ImportPackagesFromLockfileUseCaseImplTest {

    private PackageRepository packageRepository;
    private MembershipRepository membershipRepository;
    private LockfileParser lockfileParser;
    private ImportPackagesFromLockfileUseCaseImpl useCase;

    private static final String CUSTOMER_ID = UUID.randomUUID().toString();
    private static final String COMPANY_ID = UUID.randomUUID().toString();

    @BeforeEach
    void setUp() {
        packageRepository = mock(PackageRepository.class);
        membershipRepository = mock(MembershipRepository.class);
        lockfileParser = mock(LockfileParser.class);
        useCase = new ImportPackagesFromLockfileUseCaseImpl(packageRepository, membershipRepository, lockfileParser);
    }

    @Test
    void shouldImportNewPackagesFromLockfile() {
        Membership membership = createTestMembership();
        when(membershipRepository.findByCustomerIdAndCompanyId(any(CustomerId.class), any(CompanyId.class)))
                .thenReturn(Optional.of(membership));
        when(lockfileParser.supports("package-lock.json")).thenReturn(true);
        when(lockfileParser.parse(any(), eq("package-lock.json")))
                .thenReturn(List.of("lodash", "express", "react"));
        when(packageRepository.findExistingNamesByCompanyId(any(CompanyId.class), anySet()))
                .thenReturn(Set.of());

        ImportFromLockfileCommand command = new ImportFromLockfileCommand(
                COMPANY_ID, CUSTOMER_ID, "{}", "package-lock.json");

        ImportResult result = useCase.execute(command);

        assertEquals(3, result.importedCount());
        assertEquals(0, result.skippedCount());
        assertEquals(3, result.totalParsed());
        verify(packageRepository).saveAll(anyList());
    }

    @Test
    void shouldSkipExistingPackages() {
        Membership membership = createTestMembership();
        when(membershipRepository.findByCustomerIdAndCompanyId(any(CustomerId.class), any(CompanyId.class)))
                .thenReturn(Optional.of(membership));
        when(lockfileParser.supports("package-lock.json")).thenReturn(true);
        when(lockfileParser.parse(any(), eq("package-lock.json")))
                .thenReturn(List.of("lodash", "express", "react"));
        when(packageRepository.findExistingNamesByCompanyId(any(CompanyId.class), anySet()))
                .thenReturn(Set.of("lodash"));

        ImportFromLockfileCommand command = new ImportFromLockfileCommand(
                COMPANY_ID, CUSTOMER_ID, "{}", "package-lock.json");

        ImportResult result = useCase.execute(command);

        assertEquals(2, result.importedCount());
        assertEquals(1, result.skippedCount());
        assertTrue(result.skippedNames().contains("lodash"));
    }

    @Test
    void shouldThrowWhenUserNotMember() {
        when(membershipRepository.findByCustomerIdAndCompanyId(any(CustomerId.class), any(CompanyId.class)))
                .thenReturn(Optional.empty());

        ImportFromLockfileCommand command = new ImportFromLockfileCommand(
                COMPANY_ID, CUSTOMER_ID, "{}", "package-lock.json");

        assertThrows(MembershipNotFoundException.class, () -> useCase.execute(command));
    }

    @Test
    void shouldThrowForUnsupportedLockfile() {
        Membership membership = createTestMembership();
        when(membershipRepository.findByCustomerIdAndCompanyId(any(CustomerId.class), any(CompanyId.class)))
                .thenReturn(Optional.of(membership));
        when(lockfileParser.supports("unsupported.txt")).thenReturn(false);

        ImportFromLockfileCommand command = new ImportFromLockfileCommand(
                COMPANY_ID, CUSTOMER_ID, "{}", "unsupported.txt");

        assertThrows(IllegalArgumentException.class, () -> useCase.execute(command));
    }

    @Test
    void shouldNotSaveWhenAllPackagesExist() {
        Membership membership = createTestMembership();
        when(membershipRepository.findByCustomerIdAndCompanyId(any(CustomerId.class), any(CompanyId.class)))
                .thenReturn(Optional.of(membership));
        when(lockfileParser.supports("package-lock.json")).thenReturn(true);
        when(lockfileParser.parse(any(), eq("package-lock.json")))
                .thenReturn(List.of("lodash", "express"));
        when(packageRepository.findExistingNamesByCompanyId(any(CompanyId.class), anySet()))
                .thenReturn(Set.of("lodash", "express"));

        ImportFromLockfileCommand command = new ImportFromLockfileCommand(
                COMPANY_ID, CUSTOMER_ID, "{}", "package-lock.json");

        ImportResult result = useCase.execute(command);

        assertEquals(0, result.importedCount());
        assertEquals(2, result.skippedCount());
        verify(packageRepository, never()).saveAll(anyList());
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

