package com.xbank.banking_api.exception;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String resourceName, Object identifier) {
        super(resourceName + " not found: " + identifier);
    }
}
