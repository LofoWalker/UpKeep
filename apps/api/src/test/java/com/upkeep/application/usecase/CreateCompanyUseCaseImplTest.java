package com.upkeep.application.usecase;

import com.upkeep.application.port.in.CreateCompanyUseCase;
import com.upkeep.application.port.in.CreateCompanyUseCase.CreateCompanyCommand;
import com.upkeep.application.port.in.CreateCompanyUseCase.CreateCompanyResult;
import com.upkeep.application.port.out.company.CompanyRepository;
import com.upkeep.application.port.out.membership.MembershipRepository;
import com.upkeep.domain.exception.CompanySlugAlreadyExistsException;
import com.upkeep.domain.model.company.Company;
import com.upkeep.domain.model.company.CompanySlug;
import com.upkeep.domain.model.membership.Membership;
import com.upkeep.domain.model.membership.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CreateCompanyUseCaseImplTest {

    private CompanyRepository companyRepository;
    private MembershipRepository membershipRepository;
    private CreateCompanyUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        companyRepository = mock(CompanyRepository.class);
        membershipRepository = mock(MembershipRepository.class);
        useCase = new CreateCompanyUseCaseImpl(companyRepository, membershipRepository);
    }

    @Test
    void shouldCreateCompanySuccessfully() {
        String customerId = UUID.randomUUID().toString();
        String companyName = "Acme Inc";
        String companySlug = "acme-inc";

        when(companyRepository.existsBySlug(any(CompanySlug.class))).thenReturn(false);
        when(companyRepository.save(any(Company.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(membershipRepository.save(any(Membership.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CreateCompanyCommand command = new CreateCompanyCommand(customerId, companyName, companySlug);

        CreateCompanyResult result = useCase.execute(command);

        assertNotNull(result);
        assertEquals(companyName, result.name());
        assertEquals(companySlug, result.slug());
        assertNotNull(result.companyId());
        assertNotNull(result.membership());
        assertEquals(Role.OWNER, result.membership().role());

        verify(companyRepository).save(any(Company.class));
        verify(membershipRepository).save(any(Membership.class));
    }

    @Test
    void shouldGenerateSlugFromNameWhenSlugNotProvided() {
        String customerId = UUID.randomUUID().toString();
        String companyName = "My Awesome Company";

        when(companyRepository.existsBySlug(any(CompanySlug.class))).thenReturn(false);
        when(companyRepository.save(any(Company.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(membershipRepository.save(any(Membership.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CreateCompanyCommand command = new CreateCompanyCommand(customerId, companyName, null);

        CreateCompanyResult result = useCase.execute(command);

        assertNotNull(result);
        assertEquals(companyName, result.name());
        assertEquals("my-awesome-company", result.slug());
    }

    @Test
    void shouldThrowExceptionWhenSlugAlreadyExists() {
        String customerId = UUID.randomUUID().toString();
        String companyName = "Acme Inc";
        String companySlug = "acme-inc";

        when(companyRepository.existsBySlug(any(CompanySlug.class))).thenReturn(true);

        CreateCompanyCommand command = new CreateCompanyCommand(customerId, companyName, companySlug);

        assertThrows(CompanySlugAlreadyExistsException.class, () -> useCase.execute(command));

        verify(companyRepository, never()).save(any(Company.class));
        verify(membershipRepository, never()).save(any(Membership.class));
    }

    @Test
    void shouldThrowExceptionWhenCompanyNameIsTooShort() {
        String customerId = UUID.randomUUID().toString();
        String companyName = "A";

        CreateCompanyCommand command = new CreateCompanyCommand(customerId, companyName, null);

        assertThrows(IllegalArgumentException.class, () -> useCase.execute(command));
    }

    @Test
    void shouldAssignOwnerRoleToCreator() {
        String customerId = UUID.randomUUID().toString();
        String companyName = "Test Company";

        when(companyRepository.existsBySlug(any(CompanySlug.class))).thenReturn(false);
        when(companyRepository.save(any(Company.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(membershipRepository.save(any(Membership.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CreateCompanyCommand command = new CreateCompanyCommand(customerId, companyName, "test-company");

        CreateCompanyResult result = useCase.execute(command);

        assertEquals(Role.OWNER, result.membership().role());
    }
}
