package com.springboot.medical_billing.model;

import jakarta.persistence.*;
import lombok.*;

@ToString
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name="insurance_executive")
public class InsuranceExecutive {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    private String name;

    @Column(name = "contact_number")
    private String contactNumber;

    private  String email;
}
