package com.springboot.medical_billing.enums;

public enum ClaimStatus {
    SUBMITTED,
    UNDER_REVIEW,
    INITIAL_APPROVED, // cashless: TPA approved pre-auth, claim ID given to hospital (treatment can start)
    APPROVED,
    REJECTED,
    PAID // insurance company paid the approved amount (patient or hospital)
}
