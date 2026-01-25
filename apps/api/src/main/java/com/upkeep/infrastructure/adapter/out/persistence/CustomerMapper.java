package com.upkeep.infrastructure.adapter.out.persistence;

import com.upkeep.domain.model.customer.Customer;
import com.upkeep.domain.model.customer.CustomerId;
import com.upkeep.domain.model.customer.Email;
import com.upkeep.domain.model.customer.PasswordHash;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.UUID;

@Mapper(componentModel = "cdi")
public interface CustomerMapper {

    @Mapping(target = "id", source = "id.value")
    @Mapping(target = "email", source = "email.value")
    @Mapping(target = "passwordHash", source = "passwordHash.value")
    CustomerEntity toEntity(Customer customer);

    default Customer toDomain(CustomerEntity entity) {
        if (entity == null) {
            return null;
        }
        return Customer.reconstitute(
                new CustomerId(entity.id),
                new Email(entity.email),
                new PasswordHash(entity.passwordHash),
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

    default String mapPasswordHash(PasswordHash value) {
        return value != null ? value.value() : null;
    }
}
