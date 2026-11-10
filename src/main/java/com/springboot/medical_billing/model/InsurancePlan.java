package com.springboot.medical_billing.model;

import jakarta.persistence.*;
import lombok.*;

@ToString
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name="insurance_plan")
public class InsurancePlan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;

    @Column(name = "coverage_amount")
    private double coverageAmount;

    @Column(name = "premium_amount")
    private double premiumAmount;

    @Column(name = "coverage_details")
    private String coverageDetails;

    @Column(name = "terms_conditions")
    private String termsConditions;

    // insurance company that created this plan
    @ManyToOne
    @JoinColumn(name = "executive_id")
    private InsuranceExecutive executive;

}
