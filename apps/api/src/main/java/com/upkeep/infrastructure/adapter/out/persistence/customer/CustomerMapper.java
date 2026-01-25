package com.upkeep.infrastructure.adapter.out.persistence.customer;

import com.upkeep.domain.model.customer.Customer;
import com.upkeep.domain.model.customer.CustomerId;
import com.upkeep.domain.model.customer.Email;
import com.upkeep.domain.model.customer.PasswordHash;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.UUID;

@Mapper(componentModel = "cdi")
public interface CustomerMapper {

    @Mapping(target = "id", source = "id.value")
    @Mapping(target = "email", source = "email.value")
    @Mapping(target = "passwordHash", source = "customer", qualifiedByName = "extractPasswordHash")
    CustomerEntity toEntity(Customer customer);

    @Named("extractPasswordHash")
    default String extractPasswordHash(Customer customer) {
        return customer.getPasswordHash().map(PasswordHash::value).orElse(null);
    }

    default Customer toDomain(CustomerEntity entity) {
        if (entity == null) {
            return null;
        }
        PasswordHash hash = entity.passwordHash != null ? new PasswordHash(entity.passwordHash) : null;
        return Customer.reconstitute(
                new CustomerId(entity.id),
                new Email(entity.email),
                hash,
                entity.accountType,
                entity.createdAt,
                entity.updatedAt
        );
    }

    default UUID map(CustomerId value) {
        return value != null ? value.value() : null;
    }

    default String mapEmail(Email value) {
        return value != null ? value.value() : null;
    }
}
