package com.upkeep.application.usecase;

import com.upkeep.application.port.in.GetInvitationUseCase.GetInvitationQuery;
import com.upkeep.application.port.in.GetInvitationUseCase.InvitationDetails;
import com.upkeep.application.port.out.company.CompanyRepository;
import com.upkeep.application.port.out.invitation.InvitationRepository;
import com.upkeep.domain.exception.CompanyNotFoundException;
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
import com.upkeep.domain.model.membership.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("GetInvitationUseCaseImpl")
class GetInvitationUseCaseImplTest {

    private InvitationRepository invitationRepository;
    private CompanyRepository companyRepository;
    private GetInvitationUseCaseImpl useCase;

    private String token;
    private String companyId;

    @BeforeEach
    void setUp() {
        invitationRepository = mock(InvitationRepository.class);
        companyRepository = mock(CompanyRepository.class);
        useCase = new GetInvitationUseCaseImpl(invitationRepository, companyRepository);

        token = "test-token";
        companyId = UUID.randomUUID().toString();
    }

    @Test
    @DisplayName("should return invitation details successfully")
    void shouldReturnInvitationDetails() {
        Invitation invitation = createPendingInvitation(companyId, token, Role.MEMBER);
        Company company = createCompany(companyId, "Test Company", "test-company");

        when(invitationRepository.findByToken(any(InvitationToken.class)))
                .thenReturn(Optional.of(invitation));
        when(companyRepository.findById(any(CompanyId.class)))
                .thenReturn(Optional.of(company));

        GetInvitationQuery query = new GetInvitationQuery(token);

        InvitationDetails result = useCase.execute(query);

        assertNotNull(result);
        assertEquals("Test Company", result.companyName());
        assertEquals(Role.MEMBER, result.role());
        assertEquals(InvitationStatus.PENDING, result.status());
        assertFalse(result.isExpired());
        assertNotNull(result.expiresAt());
    }

    @Test
    @DisplayName("should throw InvitationNotFoundException when token not found")
    void shouldThrowWhenTokenNotFound() {
        when(invitationRepository.findByToken(any(InvitationToken.class)))
                .thenReturn(Optional.empty());

        GetInvitationQuery query = new GetInvitationQuery(token);

        assertThrows(InvitationNotFoundException.class, () -> useCase.execute(query));
    }

    @Test
    @DisplayName("should throw CompanyNotFoundException when company not found")
    void shouldThrowWhenCompanyNotFound() {
        Invitation invitation = createPendingInvitation(companyId, token, Role.MEMBER);

        when(invitationRepository.findByToken(any(InvitationToken.class)))
                .thenReturn(Optional.of(invitation));
        when(companyRepository.findById(any(CompanyId.class)))
                .thenReturn(Optional.empty());

        GetInvitationQuery query = new GetInvitationQuery(token);

        assertThrows(CompanyNotFoundException.class, () -> useCase.execute(query));
    }

    @Test
    @DisplayName("should return isExpired true for expired invitation")
    void shouldReturnIsExpiredTrueForExpiredInvitation() {
        Invitation expiredInvitation = createExpiredInvitation(companyId, token);
        Company company = createCompany(companyId, "Test Company", "test-company");

        when(invitationRepository.findByToken(any(InvitationToken.class)))
                .thenReturn(Optional.of(expiredInvitation));
        when(companyRepository.findById(any(CompanyId.class)))
                .thenReturn(Optional.of(company));

        GetInvitationQuery query = new GetInvitationQuery(token);

        InvitationDetails result = useCase.execute(query);

        assertTrue(result.isExpired());
    }

    @Test
    @DisplayName("should return correct status for accepted invitation")
    void shouldReturnCorrectStatusForAcceptedInvitation() {
        Invitation acceptedInvitation = createAcceptedInvitation(companyId, token);
        Company company = createCompany(companyId, "Test Company", "test-company");

        when(invitationRepository.findByToken(any(InvitationToken.class)))
                .thenReturn(Optional.of(acceptedInvitation));
        when(companyRepository.findById(any(CompanyId.class)))
                .thenReturn(Optional.of(company));

        GetInvitationQuery query = new GetInvitationQuery(token);

        InvitationDetails result = useCase.execute(query);

        assertEquals(InvitationStatus.ACCEPTED, result.status());
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
                Instant.now().minus(10, ChronoUnit.DAYS),
                Instant.now().minus(3, ChronoUnit.DAYS),
                Instant.now().minus(10, ChronoUnit.DAYS)
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
