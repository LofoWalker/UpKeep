package com.upkeep.application.usecase;

import com.upkeep.application.port.in.GetUserCompaniesUseCase.CompanyWithMembership;
import com.upkeep.application.port.in.GetUserCompaniesUseCase.GetUserCompaniesQuery;
import com.upkeep.application.port.out.company.CompanyRepository;
import com.upkeep.application.port.out.membership.MembershipRepository;
import com.upkeep.domain.model.company.Company;
import com.upkeep.domain.model.company.CompanyId;
import com.upkeep.domain.model.company.CompanyName;
import com.upkeep.domain.model.company.CompanySlug;
import com.upkeep.domain.model.customer.CustomerId;
import com.upkeep.domain.model.membership.Membership;
import com.upkeep.domain.model.membership.MembershipId;
import com.upkeep.domain.model.membership.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("GetUserCompaniesUseCaseImpl")
class GetUserCompaniesUseCaseImplTest {

    private MembershipRepository membershipRepository;
    private CompanyRepository companyRepository;
    private GetUserCompaniesUseCaseImpl useCase;

    private String customerId;

    @BeforeEach
    void setUp() {
        membershipRepository = mock(MembershipRepository.class);
        companyRepository = mock(CompanyRepository.class);
        useCase = new GetUserCompaniesUseCaseImpl(membershipRepository, companyRepository);

        customerId = UUID.randomUUID().toString();
    }

    @Test
    @DisplayName("should return list of companies for user")
    void shouldReturnCompaniesList() {
        String companyId1 = UUID.randomUUID().toString();
        String companyId2 = UUID.randomUUID().toString();

        Membership membership1 = createMembership(customerId, companyId1, Role.OWNER);
        Membership membership2 = createMembership(customerId, companyId2, Role.MEMBER);

        Company company1 = createCompany(companyId1, "Company One", "company-one");
        Company company2 = createCompany(companyId2, "Company Two", "company-two");

        when(membershipRepository.findAllByCustomerId(any(CustomerId.class)))
                .thenReturn(List.of(membership1, membership2));
        when(companyRepository.findById(CompanyId.from(companyId1)))
                .thenReturn(Optional.of(company1));
        when(companyRepository.findById(CompanyId.from(companyId2)))
                .thenReturn(Optional.of(company2));

        GetUserCompaniesQuery query = new GetUserCompaniesQuery(customerId);

        List<CompanyWithMembership> result = useCase.execute(query);

        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(c -> c.name().equals("Company One") && c.role() == Role.OWNER));
        assertTrue(result.stream().anyMatch(c -> c.name().equals("Company Two") && c.role() == Role.MEMBER));
    }

    @Test
    @DisplayName("should return empty list when user has no memberships")
    void shouldReturnEmptyListWhenNoMemberships() {
        when(membershipRepository.findAllByCustomerId(any(CustomerId.class)))
                .thenReturn(Collections.emptyList());

        GetUserCompaniesQuery query = new GetUserCompaniesQuery(customerId);

        List<CompanyWithMembership> result = useCase.execute(query);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("should filter out companies that no longer exist")
    void shouldFilterOutNonExistentCompanies() {
        String companyId1 = UUID.randomUUID().toString();
        String companyId2 = UUID.randomUUID().toString();

        Membership membership1 = createMembership(customerId, companyId1, Role.OWNER);
        Membership membership2 = createMembership(customerId, companyId2, Role.MEMBER);

        Company company1 = createCompany(companyId1, "Company One", "company-one");

        when(membershipRepository.findAllByCustomerId(any(CustomerId.class)))
                .thenReturn(List.of(membership1, membership2));
        when(companyRepository.findById(CompanyId.from(companyId1)))
                .thenReturn(Optional.of(company1));
        when(companyRepository.findById(CompanyId.from(companyId2)))
                .thenReturn(Optional.empty());

        GetUserCompaniesQuery query = new GetUserCompaniesQuery(customerId);

        List<CompanyWithMembership> result = useCase.execute(query);

        assertEquals(1, result.size());
        assertEquals("Company One", result.get(0).name());
    }

    @Test
    @DisplayName("should return company with correct slug")
    void shouldReturnCompanyWithCorrectSlug() {
        String companyId = UUID.randomUUID().toString();
        Membership membership = createMembership(customerId, companyId, Role.OWNER);
        Company company = createCompany(companyId, "Test Company", "test-company");

        when(membershipRepository.findAllByCustomerId(any(CustomerId.class)))
                .thenReturn(List.of(membership));
        when(companyRepository.findById(any(CompanyId.class)))
                .thenReturn(Optional.of(company));

        GetUserCompaniesQuery query = new GetUserCompaniesQuery(customerId);

        List<CompanyWithMembership> result = useCase.execute(query);

        assertEquals(1, result.size());
        assertEquals("test-company", result.get(0).slug());
    }

    private Membership createMembership(String customerId, String companyId, Role role) {
        return Membership.reconstitute(
                MembershipId.generate(),
                CustomerId.from(customerId),
                CompanyId.from(companyId),
                role,
                Instant.now(),
                Instant.now()
        );
    }

    private Company createCompany(String companyId, String name, String slug) {
        return Company.reconstitute(
                CompanyId.from(companyId),
                new CompanyName(name),
                new CompanySlug(slug),
                Instant.now(),
                Instant.now()
        );
    }
}
