package com.springboot.medical_billing.model;

import com.springboot.medical_billing.enums.PreAuthStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Entity
@Table(name = "pre_authorization")
public class PreAuthorization {

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
    @JoinColumn(name = "provider_id")
    private HealthcareProvider healthcareProvider;

    @ManyToOne
    @JoinColumn(name = "tpa_id")
    private Tpa tpa;

    private String diagnosis;

    @Column(name = "estimated_amount")
    private Double estimatedAmount;

    @Column(name = "request_date")
    private LocalDate requestDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private PreAuthStatus status;
}
