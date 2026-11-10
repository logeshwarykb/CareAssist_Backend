package com.springboot.medical_billing.model;

import com.springboot.medical_billing.enums.Gender;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Entity
@Table(name = "patient")
public class Patient {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender")
    private Gender gender;

    @Column(name = "contact_number", length = 255)
    private String contactNumber;

    @Column(name = "medical_history")
    private String medicalHistory;

    @Column(name = "invoice_file")
    private String invoiceFile;

    @Column(name = "medical_file")
    private String medicalFile;
}
