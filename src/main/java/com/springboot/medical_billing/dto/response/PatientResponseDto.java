package com.springboot.medical_billing.dto.response;
import com.springboot.medical_billing.enums.Gender;
import java.time.LocalDate;

public record PatientResponseDto(
        Integer id,
        Integer userId,
        String username,
        String fullName,
        LocalDate dateOfBirth,
        Gender gender,
        String contactNumber,
        String medicalHistory,
        String invoiceFile,
        String medicalFile
) {
}
