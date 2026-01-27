package com.upkeep.application.usecase;

import com.upkeep.application.port.in.GetCompanyMembersUseCase.GetCompanyMembersQuery;
import com.upkeep.application.port.in.GetCompanyMembersUseCase.MemberInfo;
import com.upkeep.application.port.out.customer.CustomerRepository;
import com.upkeep.application.port.out.membership.MembershipRepository;
import com.upkeep.domain.exception.MembershipNotFoundException;
import com.upkeep.domain.model.company.CompanyId;
import com.upkeep.domain.model.customer.*;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("GetCompanyMembersUseCaseImpl")
class GetCompanyMembersUseCaseImplTest {

    private MembershipRepository membershipRepository;
    private CustomerRepository customerRepository;
    private GetCompanyMembersUseCaseImpl useCase;

    private String requesterId;
    private String companyId;

    @BeforeEach
    void setUp() {
        membershipRepository = mock(MembershipRepository.class);
        customerRepository = mock(CustomerRepository.class);
        useCase = new GetCompanyMembersUseCaseImpl(membershipRepository, customerRepository);

        requesterId = UUID.randomUUID().toString();
        companyId = UUID.randomUUID().toString();
    }

    @Test
    @DisplayName("should return list of members for company")
    void shouldReturnMembersList() {
        Membership requesterMembership = createMembership(requesterId, companyId, Role.OWNER);
        String member1Id = UUID.randomUUID().toString();
        String member2Id = UUID.randomUUID().toString();
        Membership membership1 = createMembership(member1Id, companyId, Role.OWNER);
        Membership membership2 = createMembership(member2Id, companyId, Role.MEMBER);

        Customer customer1 = createCustomer(member1Id, "user1@test.com");
        Customer customer2 = createCustomer(member2Id, "user2@test.com");

        when(membershipRepository.findByCustomerIdAndCompanyId(any(CustomerId.class), any(CompanyId.class)))
                .thenReturn(Optional.of(requesterMembership));
        when(membershipRepository.findAllByCompanyId(any(CompanyId.class)))
                .thenReturn(List.of(membership1, membership2));
        when(customerRepository.findById(CustomerId.from(member1Id)))
                .thenReturn(Optional.of(customer1));
        when(customerRepository.findById(CustomerId.from(member2Id)))
                .thenReturn(Optional.of(customer2));

        GetCompanyMembersQuery query = new GetCompanyMembersQuery(requesterId, companyId);

        List<MemberInfo> result = useCase.execute(query);

        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(m -> m.email().equals("user1@test.com")));
        assertTrue(result.stream().anyMatch(m -> m.email().equals("user2@test.com")));
    }

    @Test
    @DisplayName("should throw MembershipNotFoundException when requester is not a member")
    void shouldThrowWhenRequesterNotMember() {
        when(membershipRepository.findByCustomerIdAndCompanyId(any(CustomerId.class), any(CompanyId.class)))
                .thenReturn(Optional.empty());

        GetCompanyMembersQuery query = new GetCompanyMembersQuery(requesterId, companyId);

        assertThrows(MembershipNotFoundException.class, () -> useCase.execute(query));
    }

    @Test
    @DisplayName("should return empty list when company has no members")
    void shouldReturnEmptyListWhenNoMembers() {
        Membership requesterMembership = createMembership(requesterId, companyId, Role.OWNER);

        when(membershipRepository.findByCustomerIdAndCompanyId(any(CustomerId.class), any(CompanyId.class)))
                .thenReturn(Optional.of(requesterMembership));
        when(membershipRepository.findAllByCompanyId(any(CompanyId.class)))
                .thenReturn(Collections.emptyList());

        GetCompanyMembersQuery query = new GetCompanyMembersQuery(requesterId, companyId);

        List<MemberInfo> result = useCase.execute(query);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("should return 'unknown' email when customer not found")
    void shouldReturnUnknownEmailWhenCustomerNotFound() {
        Membership requesterMembership = createMembership(requesterId, companyId, Role.OWNER);
        String memberId = UUID.randomUUID().toString();
        Membership membership = createMembership(memberId, companyId, Role.MEMBER);

        when(membershipRepository.findByCustomerIdAndCompanyId(any(CustomerId.class), any(CompanyId.class)))
                .thenReturn(Optional.of(requesterMembership));
        when(membershipRepository.findAllByCompanyId(any(CompanyId.class)))
                .thenReturn(List.of(membership));
        when(customerRepository.findById(any(CustomerId.class)))
                .thenReturn(Optional.empty());

        GetCompanyMembersQuery query = new GetCompanyMembersQuery(requesterId, companyId);

        List<MemberInfo> result = useCase.execute(query);

        assertEquals(1, result.size());
        assertEquals("unknown", result.get(0).email());
    }

    @Test
    @DisplayName("should allow MEMBER role to view company members")
    void shouldAllowMemberToViewMembers() {
        Membership requesterMembership = createMembership(requesterId, companyId, Role.MEMBER);
        Membership ownerMembership = createMembership(UUID.randomUUID().toString(), companyId, Role.OWNER);

        Customer owner = createCustomer(ownerMembership.getCustomerId().toString(), "owner@test.com");

        when(membershipRepository.findByCustomerIdAndCompanyId(any(CustomerId.class), any(CompanyId.class)))
                .thenReturn(Optional.of(requesterMembership));
        when(membershipRepository.findAllByCompanyId(any(CompanyId.class)))
                .thenReturn(List.of(ownerMembership));
        when(customerRepository.findById(any(CustomerId.class)))
                .thenReturn(Optional.of(owner));

        GetCompanyMembersQuery query = new GetCompanyMembersQuery(requesterId, companyId);

        List<MemberInfo> result = useCase.execute(query);

        assertEquals(1, result.size());
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

    private Customer createCustomer(String customerId, String email) {
        return Customer.reconstitute(
                CustomerId.from(customerId),
                new Email(email),
                new PasswordHash("hashedPassword"),
                AccountType.BOTH,
                Instant.now(),
                Instant.now()
        );
    }
}
