package com.springboot.medical_billing.model;

import com.springboot.medical_billing.enums.ProviderType;
import com.springboot.medical_billing.enums.Specialization;
import jakarta.persistence.*;
import lombok.*;

@ToString
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "healthcare_provider")
public class HealthcareProvider {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    private  String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider_type")
    private ProviderType providerType;

    @Enumerated(EnumType.STRING)
    private Specialization specialization;

    @Column(name = "contact_number")
    private String contactNumber;

    private String address;

}
