package com.springboot.medical_billing.dto.request;
import com.springboot.medical_billing.enums.Gender;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record PatientDto(
        @NotNull(message = "Name is required")
        String username,
        @NotNull(message = "Pass is required")
        @Size(min = 6, message = "Password must have at least 6 characters")
        String password,
        @NotNull(message = "FullName is required")
        String fullName,
        @NotNull(message = "DOB is required")
        @Past(message = "Date of birth must be a past date")
        LocalDate dateOfBirth,
        @NotNull(message = "Gender is required")
        Gender gender,
        @NotNull(message = "Num is required")
        @Pattern(regexp = "\\d{10}", message = "Contact number must be 10 digits")
        String contactNumber,
        String medicalHistory
) {
}
