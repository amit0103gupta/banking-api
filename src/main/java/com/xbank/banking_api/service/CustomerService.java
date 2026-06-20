package com.xbank.banking_api.service;

import com.xbank.banking_api.exception.BusinessValidationException;
import com.xbank.banking_api.exception.ResourceNotFoundException;
import com.xbank.banking_api.model.Customer;
import com.xbank.banking_api.repository.CustomerRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public Customer createCustomer(Customer customer) {
        if (customerRepository.existsByEmail(customer.getEmail())) {
            throw new BusinessValidationException(
                "Email already exists: " + customer.getEmail());
        }
        return customerRepository.save(customer);
    }

    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    public Customer getCustomerById(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", id));
    }
}
