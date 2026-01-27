package com.upkeep.application.usecase;

import com.upkeep.application.port.in.UpdateMemberRoleUseCase.UpdateMemberRoleCommand;
import com.upkeep.application.port.in.UpdateMemberRoleUseCase.UpdateMemberRoleResult;
import com.upkeep.application.port.out.membership.MembershipRepository;
import com.upkeep.domain.exception.LastOwnerException;
import com.upkeep.domain.exception.MembershipNotFoundException;
import com.upkeep.domain.exception.UnauthorizedOperationException;
import com.upkeep.domain.model.company.CompanyId;
import com.upkeep.domain.model.customer.CustomerId;
import com.upkeep.domain.model.membership.Membership;
import com.upkeep.domain.model.membership.MembershipId;
import com.upkeep.domain.model.membership.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("UpdateMemberRoleUseCaseImpl")
class UpdateMemberRoleUseCaseImplTest {

    private MembershipRepository membershipRepository;
    private UpdateMemberRoleUseCaseImpl useCase;

    private String ownerId;
    private String companyId;
    private String targetMembershipId;
    private CompanyId companyIdObj;

    @BeforeEach
    void setUp() {
        membershipRepository = mock(MembershipRepository.class);
        useCase = new UpdateMemberRoleUseCaseImpl(membershipRepository);

        ownerId = UUID.randomUUID().toString();
        companyId = UUID.randomUUID().toString();
        targetMembershipId = UUID.randomUUID().toString();
        companyIdObj = CompanyId.from(companyId);
    }

    @Test
    @DisplayName("should change member role from MEMBER to OWNER successfully")
    void shouldChangeMemberToOwner() {
        Membership ownerMembership = createMembership(ownerId, companyId, Role.OWNER);
        Membership targetMembership = createMembershipWithId(targetMembershipId, UUID.randomUUID().toString(), companyId, Role.MEMBER);

        when(membershipRepository.findByCustomerIdAndCompanyId(any(CustomerId.class), any(CompanyId.class)))
                .thenReturn(Optional.of(ownerMembership));
        when(membershipRepository.findById(any(MembershipId.class)))
                .thenReturn(Optional.of(targetMembership));
        when(membershipRepository.save(any(Membership.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        UpdateMemberRoleCommand command = new UpdateMemberRoleCommand(
                ownerId, companyId, targetMembershipId, Role.OWNER
        );

        UpdateMemberRoleResult result = useCase.execute(command);

        assertNotNull(result);
        assertEquals(targetMembershipId, result.membershipId());
        assertEquals(Role.MEMBER, result.previousRole());
        assertEquals(Role.OWNER, result.newRole());
        verify(membershipRepository).save(any(Membership.class));
    }

    @Test
    @DisplayName("should change owner role to MEMBER when multiple owners exist")
    void shouldChangeOwnerToMemberWhenMultipleOwners() {
        Membership requesterMembership = createMembership(ownerId, companyId, Role.OWNER);
        Membership targetMembership = createMembershipWithId(targetMembershipId, UUID.randomUUID().toString(), companyId, Role.OWNER);

        when(membershipRepository.findByCustomerIdAndCompanyId(any(CustomerId.class), any(CompanyId.class)))
                .thenReturn(Optional.of(requesterMembership));
        when(membershipRepository.findById(any(MembershipId.class)))
                .thenReturn(Optional.of(targetMembership));
        when(membershipRepository.countByCompanyIdAndRole(companyIdObj, Role.OWNER))
                .thenReturn(2L);
        when(membershipRepository.save(any(Membership.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        UpdateMemberRoleCommand command = new UpdateMemberRoleCommand(
                ownerId, companyId, targetMembershipId, Role.MEMBER
        );

        UpdateMemberRoleResult result = useCase.execute(command);

        assertNotNull(result);
        assertEquals(Role.OWNER, result.previousRole());
        assertEquals(Role.MEMBER, result.newRole());
    }

    @Test
    @DisplayName("should throw LastOwnerException when demoting the last owner")
    void shouldThrowLastOwnerExceptionWhenDemotingLastOwner() {
        Membership requesterMembership = createMembership(ownerId, companyId, Role.OWNER);
        Membership targetMembership = createMembershipWithId(targetMembershipId, ownerId, companyId, Role.OWNER);

        when(membershipRepository.findByCustomerIdAndCompanyId(any(CustomerId.class), any(CompanyId.class)))
                .thenReturn(Optional.of(requesterMembership));
        when(membershipRepository.findById(any(MembershipId.class)))
                .thenReturn(Optional.of(targetMembership));
        when(membershipRepository.countByCompanyIdAndRole(companyIdObj, Role.OWNER))
                .thenReturn(1L);

        UpdateMemberRoleCommand command = new UpdateMemberRoleCommand(
                ownerId, companyId, targetMembershipId, Role.MEMBER
        );

        assertThrows(LastOwnerException.class, () -> useCase.execute(command));
        verify(membershipRepository, never()).save(any(Membership.class));
    }

    @Test
    @DisplayName("should throw UnauthorizedOperationException when requester is not an owner")
    void shouldThrowUnauthorizedWhenRequesterIsNotOwner() {
        Membership memberMembership = createMembership(ownerId, companyId, Role.MEMBER);

        when(membershipRepository.findByCustomerIdAndCompanyId(any(CustomerId.class), any(CompanyId.class)))
                .thenReturn(Optional.of(memberMembership));

        UpdateMemberRoleCommand command = new UpdateMemberRoleCommand(
                ownerId, companyId, targetMembershipId, Role.OWNER
        );

        UnauthorizedOperationException exception = assertThrows(
                UnauthorizedOperationException.class,
                () -> useCase.execute(command)
        );
        assertEquals("Only owners can change member roles", exception.getMessage());
        verify(membershipRepository, never()).findById(any(MembershipId.class));
    }

    @Test
    @DisplayName("should throw MembershipNotFoundException when requester is not a member")
    void shouldThrowMembershipNotFoundWhenRequesterNotMember() {
        when(membershipRepository.findByCustomerIdAndCompanyId(any(CustomerId.class), any(CompanyId.class)))
                .thenReturn(Optional.empty());

        UpdateMemberRoleCommand command = new UpdateMemberRoleCommand(
                ownerId, companyId, targetMembershipId, Role.OWNER
        );

        assertThrows(MembershipNotFoundException.class, () -> useCase.execute(command));
    }

    @Test
    @DisplayName("should throw MembershipNotFoundException when target membership not found")
    void shouldThrowMembershipNotFoundWhenTargetNotFound() {
        Membership ownerMembership = createMembership(ownerId, companyId, Role.OWNER);

        when(membershipRepository.findByCustomerIdAndCompanyId(any(CustomerId.class), any(CompanyId.class)))
                .thenReturn(Optional.of(ownerMembership));
        when(membershipRepository.findById(any(MembershipId.class)))
                .thenReturn(Optional.empty());

        UpdateMemberRoleCommand command = new UpdateMemberRoleCommand(
                ownerId, companyId, targetMembershipId, Role.OWNER
        );

        assertThrows(MembershipNotFoundException.class, () -> useCase.execute(command));
    }

    @Test
    @DisplayName("should throw MembershipNotFoundException when target belongs to different company")
    void shouldThrowMembershipNotFoundWhenTargetBelongsToDifferentCompany() {
        String differentCompanyId = UUID.randomUUID().toString();
        Membership ownerMembership = createMembership(ownerId, companyId, Role.OWNER);
        Membership targetMembership = createMembershipWithId(targetMembershipId, UUID.randomUUID().toString(), differentCompanyId, Role.MEMBER);

        when(membershipRepository.findByCustomerIdAndCompanyId(any(CustomerId.class), any(CompanyId.class)))
                .thenReturn(Optional.of(ownerMembership));
        when(membershipRepository.findById(any(MembershipId.class)))
                .thenReturn(Optional.of(targetMembership));

        UpdateMemberRoleCommand command = new UpdateMemberRoleCommand(
                ownerId, companyId, targetMembershipId, Role.OWNER
        );

        assertThrows(MembershipNotFoundException.class, () -> useCase.execute(command));
        verify(membershipRepository, never()).save(any(Membership.class));
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

    private Membership createMembershipWithId(String membershipId, String customerId, String companyId, Role role) {
        return Membership.reconstitute(
                MembershipId.from(membershipId),
                CustomerId.from(customerId),
                CompanyId.from(companyId),
                role,
                Instant.now(),
                Instant.now()
        );
    }
}
