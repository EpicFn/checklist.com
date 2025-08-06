package com.back.domain.api.service;

import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ApiKeyService {
    public String generateApiKey() {
        return "api_" + UUID.randomUUID().toString().replace("-", "");
    }
}
