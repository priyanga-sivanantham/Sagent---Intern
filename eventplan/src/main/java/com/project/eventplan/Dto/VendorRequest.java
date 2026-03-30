package com.project.eventplan.Dto;

import jakarta.validation.constraints.NotBlank;

public record VendorRequest(
        @NotBlank(message = "Vendor name is required")
        String vendorName,
        @NotBlank(message = "Service type is required")
        String serviceType,
        String contact,
        Long userId
) {
}
