package com.upkeep.application.usecase;

import com.upkeep.application.port.in.AcceptInvitationUseCase.AcceptInvitationCommand;
import com.upkeep.application.port.in.AcceptInvitationUseCase.AcceptInvitationResult;
import com.upkeep.application.port.out.company.CompanyRepository;
import com.upkeep.application.port.out.invitation.InvitationRepository;
import com.upkeep.application.port.out.membership.MembershipRepository;
import com.upkeep.domain.exception.AlreadyMemberException;
import com.upkeep.domain.exception.CompanyNotFoundException;
import com.upkeep.domain.exception.InvitationExpiredException;
import com.upkeep.domain.exception.InvitationNotFoundException;
import com.upkeep.domain.model.company.Company;
import com.upkeep.domain.model.company.CompanyId;
import com.upkeep.domain.model.company.CompanyName;
import com.upkeep.domain.model.company.CompanySlug;
import com.upkeep.domain.model.customer.CustomerId;
import com.upkeep.domain.model.customer.Email;
import com.upkeep.domain.model.invitation.Invitation;
import com.upkeep.domain.model.invitation.InvitationId;
import com.upkeep.domain.model.invitation.InvitationStatus;
import com.upkeep.domain.model.invitation.InvitationToken;
import com.upkeep.domain.model.membership.Membership;
import com.upkeep.domain.model.membership.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
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

@DisplayName("AcceptInvitationUseCaseImpl")
class AcceptInvitationUseCaseImplTest {

    private InvitationRepository invitationRepository;
    private MembershipRepository membershipRepository;
    private CompanyRepository companyRepository;
    private AcceptInvitationUseCaseImpl useCase;

    private String customerId;
    private String token;
    private String companyId;

    @BeforeEach
    void setUp() {
        invitationRepository = mock(InvitationRepository.class);
        membershipRepository = mock(MembershipRepository.class);
        companyRepository = mock(CompanyRepository.class);
        useCase = new AcceptInvitationUseCaseImpl(invitationRepository, membershipRepository, companyRepository);

        customerId = UUID.randomUUID().toString();
        token = "test-invitation-token";
        companyId = UUID.randomUUID().toString();
    }

    @Test
    @DisplayName("should accept invitation and create membership successfully")
    void shouldAcceptInvitationSuccessfully() {
        Invitation invitation = createPendingInvitation(companyId, token, Role.MEMBER);
        Company company = createCompany(companyId, "Test Company", "test-company");

        when(invitationRepository.findByToken(any(InvitationToken.class)))
                .thenReturn(Optional.of(invitation));
        when(companyRepository.findById(any(CompanyId.class)))
                .thenReturn(Optional.of(company));
        when(membershipRepository.existsByCustomerIdAndCompanyId(any(CustomerId.class), any(CompanyId.class)))
                .thenReturn(false);
        when(invitationRepository.save(any(Invitation.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(membershipRepository.save(any(Membership.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AcceptInvitationCommand command = new AcceptInvitationCommand(customerId, token);

        AcceptInvitationResult result = useCase.execute(command);

        assertNotNull(result);
        assertEquals(companyId, result.companyId());
        assertEquals("Test Company", result.companyName());
        assertEquals("test-company", result.companySlug());
        assertEquals(Role.MEMBER, result.role());
        assertNotNull(result.membershipId());
        verify(membershipRepository).save(any(Membership.class));
        verify(invitationRepository).save(any(Invitation.class));
    }

    @Test
    @DisplayName("should throw InvitationNotFoundException when token is invalid")
    void shouldThrowInvitationNotFoundWhenTokenInvalid() {
        when(invitationRepository.findByToken(any(InvitationToken.class)))
                .thenReturn(Optional.empty());

        AcceptInvitationCommand command = new AcceptInvitationCommand(customerId, token);

        assertThrows(InvitationNotFoundException.class, () -> useCase.execute(command));
        verify(membershipRepository, never()).save(any(Membership.class));
    }

    @Test
    @DisplayName("should throw InvitationExpiredException when invitation is expired")
    void shouldThrowInvitationExpiredWhenExpired() {
        Invitation expiredInvitation = createExpiredInvitation(companyId, token);

        when(invitationRepository.findByToken(any(InvitationToken.class)))
                .thenReturn(Optional.of(expiredInvitation));
        when(invitationRepository.save(any(Invitation.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AcceptInvitationCommand command = new AcceptInvitationCommand(customerId, token);

        assertThrows(InvitationExpiredException.class, () -> useCase.execute(command));
        verify(membershipRepository, never()).save(any(Membership.class));
        verify(invitationRepository).save(any(Invitation.class));
    }

    @Test
    @DisplayName("should throw IllegalStateException when invitation is already accepted")
    void shouldThrowIllegalStateWhenInvitationAlreadyAccepted() {
        Invitation acceptedInvitation = createAcceptedInvitation(companyId, token);

        when(invitationRepository.findByToken(any(InvitationToken.class)))
                .thenReturn(Optional.of(acceptedInvitation));

        AcceptInvitationCommand command = new AcceptInvitationCommand(customerId, token);

        assertThrows(IllegalStateException.class, () -> useCase.execute(command));
        verify(membershipRepository, never()).save(any(Membership.class));
    }

    @Test
    @DisplayName("should throw AlreadyMemberException when user is already a member")
    void shouldThrowAlreadyMemberWhenUserIsAlreadyMember() {
        Invitation invitation = createPendingInvitation(companyId, token, Role.MEMBER);
        Company company = createCompany(companyId, "Test Company", "test-company");

        when(invitationRepository.findByToken(any(InvitationToken.class)))
                .thenReturn(Optional.of(invitation));
        when(companyRepository.findById(any(CompanyId.class)))
                .thenReturn(Optional.of(company));
        when(membershipRepository.existsByCustomerIdAndCompanyId(any(CustomerId.class), any(CompanyId.class)))
                .thenReturn(true);
        when(invitationRepository.save(any(Invitation.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AcceptInvitationCommand command = new AcceptInvitationCommand(customerId, token);

        assertThrows(AlreadyMemberException.class, () -> useCase.execute(command));
        verify(membershipRepository, never()).save(any(Membership.class));
    }

    @Test
    @DisplayName("should throw CompanyNotFoundException when company does not exist")
    void shouldThrowCompanyNotFoundWhenCompanyMissing() {
        Invitation invitation = createPendingInvitation(companyId, token, Role.MEMBER);

        when(invitationRepository.findByToken(any(InvitationToken.class)))
                .thenReturn(Optional.of(invitation));
        when(companyRepository.findById(any(CompanyId.class)))
                .thenReturn(Optional.empty());

        AcceptInvitationCommand command = new AcceptInvitationCommand(customerId, token);

        assertThrows(CompanyNotFoundException.class, () -> useCase.execute(command));
        verify(membershipRepository, never()).save(any(Membership.class));
    }

    @Test
    @DisplayName("should create membership with correct role from invitation")
    void shouldCreateMembershipWithCorrectRole() {
        Invitation invitation = createPendingInvitation(companyId, token, Role.OWNER);
        Company company = createCompany(companyId, "Test Company", "test-company");

        when(invitationRepository.findByToken(any(InvitationToken.class)))
                .thenReturn(Optional.of(invitation));
        when(companyRepository.findById(any(CompanyId.class)))
                .thenReturn(Optional.of(company));
        when(membershipRepository.existsByCustomerIdAndCompanyId(any(CustomerId.class), any(CompanyId.class)))
                .thenReturn(false);
        when(invitationRepository.save(any(Invitation.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(membershipRepository.save(any(Membership.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AcceptInvitationCommand command = new AcceptInvitationCommand(customerId, token);

        AcceptInvitationResult result = useCase.execute(command);

        assertEquals(Role.OWNER, result.role());
    }

    @Test
    @DisplayName("should throw IllegalStateException when invitation is declined")
    void shouldThrowIllegalStateWhenInvitationDeclined() {
        Invitation declinedInvitation = createDeclinedInvitation(companyId, token);

        when(invitationRepository.findByToken(any(InvitationToken.class)))
                .thenReturn(Optional.of(declinedInvitation));

        AcceptInvitationCommand command = new AcceptInvitationCommand(customerId, token);

        assertThrows(IllegalStateException.class, () -> useCase.execute(command));
        verify(membershipRepository, never()).save(any(Membership.class));
    }

    private Invitation createPendingInvitation(String companyId, String token, Role role) {
        return Invitation.reconstitute(
                InvitationId.generate(),
                CompanyId.from(companyId),
                CustomerId.generate(),
                new Email("invitee@test.com"),
                role,
                InvitationToken.from(token),
                InvitationStatus.PENDING,
                Instant.now(),
                Instant.now().plus(7, ChronoUnit.DAYS),
                Instant.now()
        );
    }

    private Invitation createExpiredInvitation(String companyId, String token) {
        return Invitation.reconstitute(
                InvitationId.generate(),
                CompanyId.from(companyId),
                CustomerId.generate(),
                new Email("invitee@test.com"),
                Role.MEMBER,
                InvitationToken.from(token),
                InvitationStatus.PENDING,
                Instant.now().minus(8, ChronoUnit.DAYS),
                Instant.now().minus(1, ChronoUnit.DAYS),
                Instant.now().minus(8, ChronoUnit.DAYS)
        );
    }

    private Invitation createAcceptedInvitation(String companyId, String token) {
        return Invitation.reconstitute(
                InvitationId.generate(),
                CompanyId.from(companyId),
                CustomerId.generate(),
                new Email("invitee@test.com"),
                Role.MEMBER,
                InvitationToken.from(token),
                InvitationStatus.ACCEPTED,
                Instant.now(),
                Instant.now().plus(7, ChronoUnit.DAYS),
                Instant.now()
        );
    }

    private Invitation createDeclinedInvitation(String companyId, String token) {
        return Invitation.reconstitute(
                InvitationId.generate(),
                CompanyId.from(companyId),
                CustomerId.generate(),
                new Email("invitee@test.com"),
                Role.MEMBER,
                InvitationToken.from(token),
                InvitationStatus.DECLINED,
                Instant.now(),
                Instant.now().plus(7, ChronoUnit.DAYS),
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
