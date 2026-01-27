package com.upkeep.application.usecase;

import com.upkeep.application.port.in.InviteUserToCompanyUseCase.InviteCommand;
import com.upkeep.application.port.in.InviteUserToCompanyUseCase.InviteResult;
import com.upkeep.application.port.out.invitation.InvitationRepository;
import com.upkeep.application.port.out.membership.MembershipRepository;
import com.upkeep.application.port.out.notification.EmailService;
import com.upkeep.domain.exception.DomainValidationException;
import com.upkeep.domain.exception.InvitationAlreadyExistsException;
import com.upkeep.domain.exception.MembershipNotFoundException;
import com.upkeep.domain.exception.UnauthorizedOperationException;
import com.upkeep.domain.model.company.CompanyId;
import com.upkeep.domain.model.customer.CustomerId;
import com.upkeep.domain.model.customer.Email;
import com.upkeep.domain.model.invitation.Invitation;
import com.upkeep.domain.model.invitation.InvitationStatus;
import com.upkeep.domain.model.membership.Membership;
import com.upkeep.domain.model.membership.MembershipId;
import com.upkeep.domain.model.membership.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("InviteUserToCompanyUseCaseImpl")
class InviteUserToCompanyUseCaseImplTest {

    private MembershipRepository membershipRepository;
    private InvitationRepository invitationRepository;
    private EmailService emailService;
    private InviteUserToCompanyUseCaseImpl useCase;

    private String inviterId;
    private String companyId;
    private String inviteeEmail;

    @BeforeEach
    void setUp() {
        membershipRepository = mock(MembershipRepository.class);
        invitationRepository = mock(InvitationRepository.class);
        emailService = mock(EmailService.class);
        useCase = new InviteUserToCompanyUseCaseImpl(membershipRepository, invitationRepository, emailService);

        inviterId = UUID.randomUUID().toString();
        companyId = UUID.randomUUID().toString();
        inviteeEmail = "newuser@test.com";
    }

    @Test
    @DisplayName("should create invitation and send email successfully")
    void shouldCreateInvitationSuccessfully() {
        Membership ownerMembership = createOwnerMembership(inviterId, companyId);

        when(membershipRepository.findByCustomerIdAndCompanyId(any(CustomerId.class), any(CompanyId.class)))
                .thenReturn(Optional.of(ownerMembership));
        when(invitationRepository.existsByCompanyIdAndEmailAndStatus(any(CompanyId.class), any(Email.class), eq(InvitationStatus.PENDING)))
                .thenReturn(false);
        when(invitationRepository.save(any(Invitation.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        InviteCommand command = new InviteCommand(inviterId, companyId, inviteeEmail, Role.MEMBER);

        InviteResult result = useCase.execute(command);

        assertNotNull(result);
        assertNotNull(result.invitationId());
        assertEquals(inviteeEmail, result.email());
        assertEquals(Role.MEMBER, result.role());
        assertEquals(InvitationStatus.PENDING, result.status());
        assertNotNull(result.expiresAt());

        verify(invitationRepository).save(any(Invitation.class));
        verify(emailService).sendInvitationEmail(any(Email.class), anyString());
    }

    @Test
    @DisplayName("should throw UnauthorizedOperationException when inviter is not an owner")
    void shouldThrowUnauthorizedWhenInviterNotOwner() {
        Membership memberMembership = createMemberMembership(inviterId, companyId);

        when(membershipRepository.findByCustomerIdAndCompanyId(any(CustomerId.class), any(CompanyId.class)))
                .thenReturn(Optional.of(memberMembership));

        InviteCommand command = new InviteCommand(inviterId, companyId, inviteeEmail, Role.MEMBER);

        UnauthorizedOperationException exception = assertThrows(
                UnauthorizedOperationException.class,
                () -> useCase.execute(command)
        );
        assertEquals("Only owners can invite members", exception.getMessage());
        verify(invitationRepository, never()).save(any(Invitation.class));
        verify(emailService, never()).sendInvitationEmail(any(Email.class), anyString());
    }

    @Test
    @DisplayName("should throw MembershipNotFoundException when inviter is not a member")
    void shouldThrowMembershipNotFoundWhenInviterNotMember() {
        when(membershipRepository.findByCustomerIdAndCompanyId(any(CustomerId.class), any(CompanyId.class)))
                .thenReturn(Optional.empty());

        InviteCommand command = new InviteCommand(inviterId, companyId, inviteeEmail, Role.MEMBER);

        assertThrows(MembershipNotFoundException.class, () -> useCase.execute(command));
        verify(invitationRepository, never()).save(any(Invitation.class));
    }

    @Test
    @DisplayName("should throw InvitationAlreadyExistsException when pending invitation exists")
    void shouldThrowInvitationAlreadyExistsWhenPendingExists() {
        Membership ownerMembership = createOwnerMembership(inviterId, companyId);

        when(membershipRepository.findByCustomerIdAndCompanyId(any(CustomerId.class), any(CompanyId.class)))
                .thenReturn(Optional.of(ownerMembership));
        when(invitationRepository.existsByCompanyIdAndEmailAndStatus(any(CompanyId.class), any(Email.class), eq(InvitationStatus.PENDING)))
                .thenReturn(true);

        InviteCommand command = new InviteCommand(inviterId, companyId, inviteeEmail, Role.MEMBER);

        InvitationAlreadyExistsException exception = assertThrows(
                InvitationAlreadyExistsException.class,
                () -> useCase.execute(command)
        );
        assertEquals(inviteeEmail, exception.getEmail());
        verify(invitationRepository, never()).save(any(Invitation.class));
    }

    @Test
    @DisplayName("should create invitation with OWNER role")
    void shouldCreateInvitationWithOwnerRole() {
        Membership ownerMembership = createOwnerMembership(inviterId, companyId);

        when(membershipRepository.findByCustomerIdAndCompanyId(any(CustomerId.class), any(CompanyId.class)))
                .thenReturn(Optional.of(ownerMembership));
        when(invitationRepository.existsByCompanyIdAndEmailAndStatus(any(CompanyId.class), any(Email.class), eq(InvitationStatus.PENDING)))
                .thenReturn(false);
        when(invitationRepository.save(any(Invitation.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        InviteCommand command = new InviteCommand(inviterId, companyId, inviteeEmail, Role.OWNER);

        InviteResult result = useCase.execute(command);

        assertEquals(Role.OWNER, result.role());
    }

    @Test
    @DisplayName("should send invitation email with correct token")
    void shouldSendEmailWithCorrectToken() {
        Membership ownerMembership = createOwnerMembership(inviterId, companyId);

        when(membershipRepository.findByCustomerIdAndCompanyId(any(CustomerId.class), any(CompanyId.class)))
                .thenReturn(Optional.of(ownerMembership));
        when(invitationRepository.existsByCompanyIdAndEmailAndStatus(any(CompanyId.class), any(Email.class), eq(InvitationStatus.PENDING)))
                .thenReturn(false);
        when(invitationRepository.save(any(Invitation.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        InviteCommand command = new InviteCommand(inviterId, companyId, inviteeEmail, Role.MEMBER);

        useCase.execute(command);

        ArgumentCaptor<Email> emailCaptor = ArgumentCaptor.forClass(Email.class);
        ArgumentCaptor<String> tokenCaptor = ArgumentCaptor.forClass(String.class);
        verify(emailService).sendInvitationEmail(emailCaptor.capture(), tokenCaptor.capture());

        assertEquals(inviteeEmail, emailCaptor.getValue().value());
        assertNotNull(tokenCaptor.getValue());
    }

    @Test
    @DisplayName("should throw IllegalArgumentException for invalid email format")
    void shouldThrowIllegalArgumentForInvalidEmail() {
        Membership ownerMembership = createOwnerMembership(inviterId, companyId);

        when(membershipRepository.findByCustomerIdAndCompanyId(any(CustomerId.class), any(CompanyId.class)))
                .thenReturn(Optional.of(ownerMembership));

        InviteCommand command = new InviteCommand(inviterId, companyId, "invalid-email", Role.MEMBER);

        assertThrows(DomainValidationException.class, () -> useCase.execute(command));
    }

    private Membership createOwnerMembership(String customerId, String companyId) {
        return Membership.reconstitute(
                MembershipId.generate(),
                CustomerId.from(customerId),
                CompanyId.from(companyId),
                Role.OWNER,
                Instant.now(),
                Instant.now()
        );
    }

    private Membership createMemberMembership(String customerId, String companyId) {
        return Membership.reconstitute(
                MembershipId.generate(),
                CustomerId.from(customerId),
                CompanyId.from(companyId),
                Role.MEMBER,
                Instant.now(),
                Instant.now()
        );
    }
}
