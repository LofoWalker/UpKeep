package com.upkeep.application.usecase;

import com.upkeep.application.port.in.pkg.ListCompanyPackagesUseCase.ListPackagesQuery;
import com.upkeep.application.port.in.pkg.ListCompanyPackagesUseCase.PackageListResult;
import com.upkeep.application.port.out.membership.MembershipRepository;
import com.upkeep.application.port.out.pkg.PackageRepository;
import com.upkeep.domain.exception.MembershipNotFoundException;
import com.upkeep.domain.model.company.CompanyId;
import com.upkeep.domain.model.customer.CustomerId;
import com.upkeep.domain.model.membership.Membership;
import com.upkeep.domain.model.membership.MembershipId;
import com.upkeep.domain.model.membership.Role;
import com.upkeep.domain.model.pkg.Package;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ListCompanyPackagesUseCaseImplTest {

    private PackageRepository packageRepository;
    private MembershipRepository membershipRepository;
    private ListCompanyPackagesUseCaseImpl useCase;

    private static final String CUSTOMER_ID = UUID.randomUUID().toString();
    private static final String COMPANY_ID = UUID.randomUUID().toString();

    @BeforeEach
    void setUp() {
        packageRepository = mock(PackageRepository.class);
        membershipRepository = mock(MembershipRepository.class);
        useCase = new ListCompanyPackagesUseCaseImpl(packageRepository, membershipRepository);
    }

    @Test
    void shouldListPackagesWithPagination() {
        Membership membership = createTestMembership();
        when(membershipRepository.findByCustomerIdAndCompanyId(any(CustomerId.class), any(CompanyId.class)))
                .thenReturn(Optional.of(membership));

        CompanyId companyId = CompanyId.from(COMPANY_ID);
        Package pkg = Package.reconstitute(
                com.upkeep.domain.model.pkg.PackageId.generate(),
                companyId, "lodash", "npm", Instant.now());

        when(packageRepository.findByCompanyId(any(CompanyId.class), anyInt(), anyInt()))
                .thenReturn(List.of(pkg));
        when(packageRepository.countByCompanyId(any(CompanyId.class))).thenReturn(1L);

        ListPackagesQuery query = new ListPackagesQuery(COMPANY_ID, CUSTOMER_ID, null, 0, 50);

        PackageListResult result = useCase.execute(query);

        assertEquals(1, result.packages().size());
        assertEquals("lodash", result.packages().get(0).name());
        assertEquals(1L, result.totalCount());
    }

    @Test
    void shouldFilterBySearch() {
        Membership membership = createTestMembership();
        when(membershipRepository.findByCustomerIdAndCompanyId(any(CustomerId.class), any(CompanyId.class)))
                .thenReturn(Optional.of(membership));
        when(packageRepository.findByCompanyIdAndNameContaining(any(CompanyId.class), eq("react"), anyInt(), anyInt()))
                .thenReturn(List.of());
        when(packageRepository.countByCompanyIdAndNameContaining(any(CompanyId.class), eq("react"))).thenReturn(0L);

        ListPackagesQuery query = new ListPackagesQuery(COMPANY_ID, CUSTOMER_ID, "react", 0, 50);

        PackageListResult result = useCase.execute(query);

        assertEquals(0, result.packages().size());
        verify(packageRepository).findByCompanyIdAndNameContaining(any(CompanyId.class), eq("react"), anyInt(), anyInt());
    }

    @Test
    void shouldThrowWhenUserNotMember() {
        when(membershipRepository.findByCustomerIdAndCompanyId(any(CustomerId.class), any(CompanyId.class)))
                .thenReturn(Optional.empty());

        ListPackagesQuery query = new ListPackagesQuery(COMPANY_ID, CUSTOMER_ID, null, 0, 50);

        assertThrows(MembershipNotFoundException.class, () -> useCase.execute(query));
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

