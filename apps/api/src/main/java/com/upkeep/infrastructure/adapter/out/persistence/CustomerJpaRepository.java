package com.upkeep.infrastructure.adapter.out.persistence;

import com.upkeep.application.port.out.CustomerRepository;
import com.upkeep.domain.model.customer.Customer;
import com.upkeep.domain.model.customer.CustomerId;
import com.upkeep.domain.model.customer.Email;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.Optional;

@ApplicationScoped
public class CustomerJpaRepository implements CustomerRepository {

    private final CustomerMapper customerMapper;

    @Inject
    public CustomerJpaRepository(CustomerMapper customerMapper) {
        this.customerMapper = customerMapper;
    }

    @Override
    public void save(Customer customer) {
        CustomerEntity entity = customerMapper.toEntity(customer);
        entity.persist();
    }

    @Override
    public Optional<Customer> findById(CustomerId id) {
        return CustomerEntity.<CustomerEntity>findByIdOptional(id.value())
                .map(customerMapper::toDomain);
    }

    @Override
    public Optional<Customer> findByEmail(Email email) {
        return CustomerEntity.<CustomerEntity>find("email", email.value())
                .firstResultOptional()
                .map(customerMapper::toDomain);
    }

    @Override
    public boolean existsByEmail(Email email) {
        return CustomerEntity.count("email", email.value()) > 0;
    }
}
