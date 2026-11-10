package com.springboot.medical_billing.model;


import com.springboot.medical_billing.enums.ClaimStatus;
import com.springboot.medical_billing.enums.ClaimType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Entity
@Table(name = "claim")
public class Claim {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "patient_id")
    private Patient patient;

    @ManyToOne
    @JoinColumn(name = "policy_id")
    private PatientPolicy policy;

    @ManyToOne
    @JoinColumn(name = "executive_id")
    private InsuranceExecutive executive;

    @Column(name = "claim_date")
    private LocalDate claimDate;

    @Column(name = "claim_amount")
    private Double claimAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private ClaimStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "claim_type")
    private ClaimType claimType;

    @ManyToOne
    @JoinColumn(name = "tpa_id")
    private Tpa tpa;

    @ManyToOne
    @JoinColumn(name = "pre_auth_id")
    private PreAuthorization preAuthorization;

    @Column(name = "approved_amount")
    private Double approvedAmount;

    @Column(name = "invoice_file")
    private String invoiceFile;

    @Column(name = "medical_file")
    private String medicalFile;

    // date when TPA / insurance company approved the claim
    @Column(name = "approved_date")
    private LocalDate approvedDate;

}
