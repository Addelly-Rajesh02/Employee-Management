package com.example.employee.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record EmployeeResponse(
        UUID id,
        String firstName,
        String lastName,
        String email,
        String department,
        BigDecimal salary,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {}
