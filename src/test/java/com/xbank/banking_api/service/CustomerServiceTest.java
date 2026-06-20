package com.xbank.banking_api.service;

import com.xbank.banking_api.model.Customer;
import com.xbank.banking_api.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerService customerService;

    private Customer customer;

    @BeforeEach
    void setUp() {
        customer = new Customer();
        customer.setId(1L);
        customer.setFirstName("John");
        customer.setLastName("Doe");
        customer.setEmail("john@example.com");
        customer.setPhone("1234567890");
        customer.setAddress("123 Main St");
    }

    @Test
    void createCustomer_success() {
        when(customerRepository.existsByEmail("john@example.com")).thenReturn(false);
        when(customerRepository.save(any(Customer.class))).thenReturn(customer);

        Customer result = customerService.createCustomer(customer);

        assertNotNull(result);
        assertEquals("John", result.getFirstName());
        assertEquals("john@example.com", result.getEmail());
        verify(customerRepository).existsByEmail("john@example.com");
        verify(customerRepository).save(customer);
    }

    @Test
    void createCustomer_duplicateEmail_throwsConflict() {
        when(customerRepository.existsByEmail("john@example.com")).thenReturn(true);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> customerService.createCustomer(customer)
        );

        assertEquals(409, exception.getStatusCode().value());
        assertTrue(exception.getReason().contains("Email already exists"));
        verify(customerRepository, never()).save(any());
    }

    @Test
    void getAllCustomers_returnsList() {
        Customer customer2 = new Customer();
        customer2.setId(2L);
        customer2.setFirstName("Jane");
        customer2.setLastName("Doe");
        customer2.setEmail("jane@example.com");

        when(customerRepository.findAll()).thenReturn(Arrays.asList(customer, customer2));

        List<Customer> result = customerService.getAllCustomers();

        assertEquals(2, result.size());
        verify(customerRepository).findAll();
    }

    @Test
    void getAllCustomers_emptyList() {
        when(customerRepository.findAll()).thenReturn(List.of());

        List<Customer> result = customerService.getAllCustomers();

        assertTrue(result.isEmpty());
    }

    @Test
    void getCustomerById_found() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));

        Customer result = customerService.getCustomerById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("John", result.getFirstName());
    }

    @Test
    void getCustomerById_notFound_throwsNotFound() {
        when(customerRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> customerService.getCustomerById(99L)
        );

        assertEquals(404, exception.getStatusCode().value());
        assertTrue(exception.getReason().contains("Customer not found"));
    }
}
