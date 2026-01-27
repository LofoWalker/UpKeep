package com.upkeep.application.usecase;

import com.upkeep.application.port.in.GetCompanyDashboardUseCase.CompanyDashboard;
import com.upkeep.application.port.in.GetCompanyDashboardUseCase.GetCompanyDashboardQuery;
import com.upkeep.application.port.out.company.CompanyRepository;
import com.upkeep.application.port.out.membership.MembershipRepository;
import com.upkeep.domain.exception.CompanyNotFoundException;
import com.upkeep.domain.exception.MembershipNotFoundException;
import com.upkeep.domain.model.company.Company;
import com.upkeep.domain.model.company.CompanyId;
import com.upkeep.domain.model.company.CompanyName;
import com.upkeep.domain.model.company.CompanySlug;
import com.upkeep.domain.model.customer.CustomerId;
import com.upkeep.domain.model.membership.Membership;
import com.upkeep.domain.model.membership.MembershipId;
import com.upkeep.domain.model.membership.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GetCompanyDashboardUseCaseImplTest {

    private CompanyRepository companyRepository;
    private MembershipRepository membershipRepository;
    private GetCompanyDashboardUseCaseImpl useCase;

    private static final String CUSTOMER_ID = UUID.randomUUID().toString();
    private static final String COMPANY_ID = UUID.randomUUID().toString();

    @BeforeEach
    void setUp() {
        companyRepository = mock(CompanyRepository.class);
        membershipRepository = mock(MembershipRepository.class);
        useCase = new GetCompanyDashboardUseCaseImpl(companyRepository, membershipRepository);
    }

    @Test
    void shouldReturnDashboardSuccessfully() {
        Company company = createTestCompany();
        Membership membership = createTestMembership(Role.OWNER);
        List<Membership> allMembers = List.of(membership, createTestMembership(Role.MEMBER));

        when(companyRepository.findById(any(CompanyId.class))).thenReturn(Optional.of(company));
        when(membershipRepository.findByCustomerIdAndCompanyId(any(CustomerId.class), any(CompanyId.class)))
                .thenReturn(Optional.of(membership));
        when(membershipRepository.findAllByCompanyId(any(CompanyId.class))).thenReturn(allMembers);

        GetCompanyDashboardQuery query = new GetCompanyDashboardQuery(CUSTOMER_ID, COMPANY_ID);

        CompanyDashboard result = useCase.execute(query);

        assertNotNull(result);
        assertEquals("Acme Inc", result.name());
        assertEquals("acme-inc", result.slug());
        assertEquals(Role.OWNER, result.userRole());
        assertEquals(2, result.stats().totalMembers());
        assertFalse(result.stats().hasBudget());
        assertFalse(result.stats().hasPackages());
        assertFalse(result.stats().hasAllocations());
    }

    @Test
    void shouldThrowWhenCompanyNotFound() {
        when(companyRepository.findById(any(CompanyId.class))).thenReturn(Optional.empty());

        GetCompanyDashboardQuery query = new GetCompanyDashboardQuery(CUSTOMER_ID, COMPANY_ID);

        CompanyNotFoundException exception = assertThrows(
                CompanyNotFoundException.class,
                () -> useCase.execute(query)
        );

        assertNotNull(exception);
    }

    @Test
    void shouldThrowWhenUserNotMember() {
        Company company = createTestCompany();

        when(companyRepository.findById(any(CompanyId.class))).thenReturn(Optional.of(company));
        when(membershipRepository.findByCustomerIdAndCompanyId(any(CustomerId.class), any(CompanyId.class)))
                .thenReturn(Optional.empty());

        GetCompanyDashboardQuery query = new GetCompanyDashboardQuery(CUSTOMER_ID, COMPANY_ID);

        MembershipNotFoundException exception = assertThrows(
                MembershipNotFoundException.class,
                () -> useCase.execute(query)
        );

        assertNotNull(exception);
    }

    @Test
    void shouldReturnCorrectTotalMembers() {
        Company company = createTestCompany();
        Membership membership = createTestMembership(Role.MEMBER);
        List<Membership> allMembers = List.of(
                createTestMembership(Role.OWNER),
                createTestMembership(Role.MEMBER),
                createTestMembership(Role.MEMBER),
                createTestMembership(Role.MEMBER)
        );

        when(companyRepository.findById(any(CompanyId.class))).thenReturn(Optional.of(company));
        when(membershipRepository.findByCustomerIdAndCompanyId(any(CustomerId.class), any(CompanyId.class)))
                .thenReturn(Optional.of(membership));
        when(membershipRepository.findAllByCompanyId(any(CompanyId.class))).thenReturn(allMembers);

        GetCompanyDashboardQuery query = new GetCompanyDashboardQuery(CUSTOMER_ID, COMPANY_ID);

        CompanyDashboard result = useCase.execute(query);

        assertEquals(4, result.stats().totalMembers());
    }

    @Test
    void shouldReturnCorrectUserRoleForMember() {
        Company company = createTestCompany();
        Membership membership = createTestMembership(Role.MEMBER);

        when(companyRepository.findById(any(CompanyId.class))).thenReturn(Optional.of(company));
        when(membershipRepository.findByCustomerIdAndCompanyId(any(CustomerId.class), any(CompanyId.class)))
                .thenReturn(Optional.of(membership));
        when(membershipRepository.findAllByCompanyId(any(CompanyId.class))).thenReturn(List.of(membership));

        GetCompanyDashboardQuery query = new GetCompanyDashboardQuery(CUSTOMER_ID, COMPANY_ID);

        CompanyDashboard result = useCase.execute(query);

        assertEquals(Role.MEMBER, result.userRole());
    }

    private Company createTestCompany() {
        return Company.reconstitute(
                CompanyId.from(COMPANY_ID),
                new CompanyName("Acme Inc"),
                new CompanySlug("acme-inc"),
                Instant.now(),
                Instant.now()
        );
    }

    private Membership createTestMembership(Role role) {
        return Membership.reconstitute(
                MembershipId.from(UUID.randomUUID()),
                CustomerId.from(CUSTOMER_ID),
                CompanyId.from(COMPANY_ID),
                role,
                Instant.now(),
                Instant.now()
        );
    }
}
