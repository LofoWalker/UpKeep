package com.upkeep.application.port.out;

import com.upkeep.domain.model.customer.Customer;
import com.upkeep.domain.model.customer.CustomerId;
import com.upkeep.domain.model.customer.Email;

import java.util.Optional;

public interface CustomerRepository {
    void save(Customer customer);

    Optional<Customer> findById(CustomerId id);

    Optional<Customer> findByEmail(Email email);

    boolean existsByEmail(Email email);
}
