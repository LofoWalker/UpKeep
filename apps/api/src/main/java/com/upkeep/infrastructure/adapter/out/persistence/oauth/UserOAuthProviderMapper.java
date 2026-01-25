package com.upkeep.infrastructure.adapter.out.persistence.oauth;

import com.upkeep.domain.model.customer.CustomerId;
import com.upkeep.domain.model.oauth.UserOAuthProvider;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.UUID;

@Mapper(componentModel = "cdi")
public interface UserOAuthProviderMapper {

    @Mapping(target = "userId", source = "userId.value")
    UserOAuthProviderEntity toEntity(UserOAuthProvider domain);

    default UserOAuthProvider toDomain(UserOAuthProviderEntity entity) {
        if (entity == null) {
            return null;
        }
        return UserOAuthProvider.reconstitute(
                entity.id,
                new CustomerId(entity.userId),
                entity.provider,
                entity.providerUserId,
                entity.providerEmail,
                entity.createdAt
        );
    }

    default UUID map(CustomerId value) {
        return value != null ? value.value() : null;
    }
}
