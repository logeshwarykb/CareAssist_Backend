package com.springboot.medical_billing.enums;

public enum ClaimType {
    REIMBURSEMENT, // patient pays the hospital and claims the money back
    CASHLESS       // hospital sends the claim through TPA, insurance pays the hospital
}
