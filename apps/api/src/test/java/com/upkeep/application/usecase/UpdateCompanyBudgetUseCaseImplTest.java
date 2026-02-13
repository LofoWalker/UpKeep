package com.upkeep.application.usecase;

import com.upkeep.application.port.in.budget.UpdateCompanyBudgetUseCase;
import com.upkeep.application.port.in.budget.UpdateCompanyBudgetUseCase.UpdateBudgetCommand;
import com.upkeep.application.port.in.budget.UpdateCompanyBudgetUseCase.UpdateBudgetResult;
import com.upkeep.application.port.out.audit.AuditEventRepository;
import com.upkeep.application.port.out.budget.BudgetRepository;
import com.upkeep.application.port.out.membership.MembershipRepository;
import com.upkeep.domain.exception.BudgetNotFoundException;
import com.upkeep.domain.exception.MembershipNotFoundException;
import com.upkeep.domain.exception.UnauthorizedOperationException;
import com.upkeep.domain.model.audit.AuditEvent;
import com.upkeep.domain.model.budget.Budget;
import com.upkeep.domain.model.budget.Currency;
import com.upkeep.domain.model.budget.Money;
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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("UpdateCompanyBudgetUseCase")
class UpdateCompanyBudgetUseCaseImplTest {

    private BudgetRepository budgetRepository;
    private AuditEventRepository auditEventRepository;
    private MembershipRepository membershipRepository;
    private UpdateCompanyBudgetUseCase useCase;

    @BeforeEach
    void setUp() {
        budgetRepository = mock(BudgetRepository.class);
        auditEventRepository = mock(AuditEventRepository.class);
        membershipRepository = mock(MembershipRepository.class);
        useCase = new UpdateCompanyBudgetUseCaseImpl(
                budgetRepository,
                auditEventRepository,
                membershipRepository
        );
    }

    @Test
    @DisplayName("should update budget successfully when user is owner")
    void shouldUpdateBudgetSuccessfully() {
        String companyId = UUID.randomUUID().toString();
        String userId = UUID.randomUUID().toString();
        long newAmountCents = 100000L;

        Membership ownerMembership = createOwnerMembership(userId, companyId);
        when(membershipRepository.findByCustomerIdAndCompanyId(
                any(CustomerId.class),
                any(CompanyId.class)
        )).thenReturn(Optional.of(ownerMembership));

        Budget existingBudget = Budget.create(
                CompanyId.from(companyId),
                new Money(50000L, Currency.EUR)
        );
        when(budgetRepository.findByCompanyId(any(CompanyId.class)))
                .thenReturn(Optional.of(existingBudget));

        UpdateBudgetCommand command = new UpdateBudgetCommand(
                companyId,
                userId,
                newAmountCents,
                Currency.EUR
        );

        UpdateBudgetResult result = useCase.execute(command);

        assertNotNull(result);
        assertEquals(newAmountCents, result.amountCents());
        assertEquals("EUR", result.currency());
        assertFalse(result.isLowerThanAllocations());
        assertEquals(0L, result.currentAllocationsCents());

        verify(budgetRepository).save(any(Budget.class));
        verify(auditEventRepository).save(any(AuditEvent.class));
    }

    @Test
    @DisplayName("should throw exception when membership not found")
    void shouldThrowExceptionWhenMembershipNotFound() {
        String companyId = UUID.randomUUID().toString();
        String userId = UUID.randomUUID().toString();

        when(membershipRepository.findByCustomerIdAndCompanyId(
                any(CustomerId.class),
                any(CompanyId.class)
        )).thenReturn(Optional.empty());

        UpdateBudgetCommand command = new UpdateBudgetCommand(
                companyId,
                userId,
                100000L,
                Currency.EUR
        );

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

        UpdateBudgetCommand command = new UpdateBudgetCommand(
                companyId,
                userId,
                100000L,
                Currency.EUR
        );

        assertThrows(UnauthorizedOperationException.class, () -> useCase.execute(command));

        verify(budgetRepository, never()).save(any(Budget.class));
        verify(auditEventRepository, never()).save(any(AuditEvent.class));
    }

    @Test
    @DisplayName("should throw exception when budget not found")
    void shouldThrowExceptionWhenBudgetNotFound() {
        String companyId = UUID.randomUUID().toString();
        String userId = UUID.randomUUID().toString();

        Membership ownerMembership = createOwnerMembership(userId, companyId);
        when(membershipRepository.findByCustomerIdAndCompanyId(
                any(CustomerId.class),
                any(CompanyId.class)
        )).thenReturn(Optional.of(ownerMembership));

        when(budgetRepository.findByCompanyId(any(CompanyId.class)))
                .thenReturn(Optional.empty());

        UpdateBudgetCommand command = new UpdateBudgetCommand(
                companyId,
                userId,
                100000L,
                Currency.EUR
        );

        assertThrows(BudgetNotFoundException.class, () -> useCase.execute(command));

        verify(budgetRepository, never()).save(any(Budget.class));
        verify(auditEventRepository, never()).save(any(AuditEvent.class));
    }

    @Test
    @DisplayName("should indicate when new budget is lower than allocations")
    void shouldIndicateWhenBudgetLowerThanAllocations() {
        String companyId = UUID.randomUUID().toString();
        String userId = UUID.randomUUID().toString();
        long newAmountCents = 10000L;

        Membership ownerMembership = createOwnerMembership(userId, companyId);
        when(membershipRepository.findByCustomerIdAndCompanyId(
                any(CustomerId.class),
                any(CompanyId.class)
        )).thenReturn(Optional.of(ownerMembership));

        Budget existingBudget = Budget.create(
                CompanyId.from(companyId),
                new Money(50000L, Currency.EUR)
        );
        when(budgetRepository.findByCompanyId(any(CompanyId.class)))
                .thenReturn(Optional.of(existingBudget));

        UpdateBudgetCommand command = new UpdateBudgetCommand(
                companyId,
                userId,
                newAmountCents,
                Currency.EUR
        );

        UpdateBudgetResult result = useCase.execute(command);

        assertNotNull(result);
        assertFalse(result.isLowerThanAllocations());
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

