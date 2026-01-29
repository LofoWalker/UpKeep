package com.upkeep.application.usecase;

import com.upkeep.application.port.in.budget.SetCompanyBudgetUseCase;
import com.upkeep.application.port.in.budget.SetCompanyBudgetUseCase.SetBudgetCommand;
import com.upkeep.application.port.in.budget.SetCompanyBudgetUseCase.SetBudgetResult;
import com.upkeep.application.port.out.audit.AuditEventRepository;
import com.upkeep.application.port.out.budget.BudgetRepository;
import com.upkeep.application.port.out.membership.MembershipRepository;
import com.upkeep.domain.exception.MembershipNotFoundException;
import com.upkeep.domain.exception.UnauthorizedOperationException;
import com.upkeep.domain.model.audit.AuditEvent;
import com.upkeep.domain.model.budget.Budget;
import com.upkeep.domain.model.budget.Currency;
import com.upkeep.domain.model.company.CompanyId;
import com.upkeep.domain.model.customer.CustomerId;
import com.upkeep.domain.model.membership.Membership;
import com.upkeep.domain.model.membership.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

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

@DisplayName("SetCompanyBudgetUseCase")
class SetCompanyBudgetUseCaseImplTest {

    private BudgetRepository budgetRepository;
    private AuditEventRepository auditEventRepository;
    private MembershipRepository membershipRepository;
    private SetCompanyBudgetUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        budgetRepository = mock(BudgetRepository.class);
        auditEventRepository = mock(AuditEventRepository.class);
        membershipRepository = mock(MembershipRepository.class);
        useCase = new SetCompanyBudgetUseCaseImpl(
            budgetRepository,
            auditEventRepository,
            membershipRepository
        );
    }

    @Test
    @DisplayName("should set budget successfully when user is owner")
    void shouldSetBudgetSuccessfully() {
        String companyId = UUID.randomUUID().toString();
        String userId = UUID.randomUUID().toString();
        long amountCents = 50000L;
        Currency currency = Currency.EUR;

        Membership ownerMembership = createOwnerMembership(userId, companyId);
        when(membershipRepository.findByCustomerIdAndCompanyId(
            any(CustomerId.class),
            any(CompanyId.class)
        )).thenReturn(Optional.of(ownerMembership));

        SetBudgetCommand command = new SetBudgetCommand(companyId, userId, amountCents, currency);

        SetBudgetResult result = useCase.execute(command);

        assertNotNull(result);
        assertNotNull(result.budgetId());
        assertEquals(amountCents, result.amountCents());
        assertEquals(currency.name(), result.currency());

        verify(budgetRepository).save(any(Budget.class));
        verify(auditEventRepository).save(any(AuditEvent.class));
    }

    @Test
    @DisplayName("should throw exception when user is not a member")
    void shouldThrowExceptionWhenNotMember() {
        String companyId = UUID.randomUUID().toString();
        String userId = UUID.randomUUID().toString();

        when(membershipRepository.findByCustomerIdAndCompanyId(
            any(CustomerId.class),
            any(CompanyId.class)
        )).thenReturn(Optional.empty());

        SetBudgetCommand command = new SetBudgetCommand(companyId, userId, 50000L, Currency.EUR);

        assertThrows(MembershipNotFoundException.class, () -> useCase.execute(command));

        verify(budgetRepository, never()).save(any(Budget.class));
        verify(auditEventRepository, never()).save(any(AuditEvent.class));
    }

    @Test
    @DisplayName("should throw exception when user is not owner")
    void shouldThrowExceptionWhenNotOwner() {
        String companyId = UUID.randomUUID().toString();
        String userId = UUID.randomUUID().toString();

        Membership memberMembership = createMemberMembership(userId, companyId);
        when(membershipRepository.findByCustomerIdAndCompanyId(
            any(CustomerId.class),
            any(CompanyId.class)
        )).thenReturn(Optional.of(memberMembership));

        SetBudgetCommand command = new SetBudgetCommand(companyId, userId, 50000L, Currency.EUR);

        assertThrows(UnauthorizedOperationException.class, () -> useCase.execute(command));

        verify(budgetRepository, never()).save(any(Budget.class));
        verify(auditEventRepository, never()).save(any(AuditEvent.class));
    }

    @Test
    @DisplayName("should set budget with different currencies")
    void shouldSetBudgetWithDifferentCurrencies() {
        String companyId = UUID.randomUUID().toString();
        String userId = UUID.randomUUID().toString();

        Membership ownerMembership = createOwnerMembership(userId, companyId);
        when(membershipRepository.findByCustomerIdAndCompanyId(
            any(CustomerId.class),
            any(CompanyId.class)
        )).thenReturn(Optional.of(ownerMembership));

        SetBudgetCommand command = new SetBudgetCommand(companyId, userId, 100000L, Currency.USD);

        SetBudgetResult result = useCase.execute(command);

        assertEquals("USD", result.currency());
        assertEquals(100000L, result.amountCents());
    }

    private Membership createOwnerMembership(String userId, String companyId) {
        return Membership.create(
            CustomerId.from(userId),
            CompanyId.from(companyId),
            Role.OWNER
        );
    }

    private Membership createMemberMembership(String userId, String companyId) {
        return Membership.create(
            CustomerId.from(userId),
            CompanyId.from(companyId),
            Role.MEMBER
        );
    }
}
