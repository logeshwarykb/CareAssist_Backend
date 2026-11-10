package com.springboot.medical_billing.dto.request;
import com.springboot.medical_billing.enums.Gender;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;

import java.time.LocalDate;

public record PatientUpdateDto(
        @NotNull(message = "Name is required")
        String fullName,
        @NotNull(message = "DOB is required")
        @Past(message = "Date of birth must be a past date")
        LocalDate dateOfBirth,
        @NotNull(message = "Gender is required")
        Gender gender,
        @NotNull(message = "Num is required")
        String contactNumber,

        String medicalHistory
) {
}
