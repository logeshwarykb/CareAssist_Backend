package com.springboot.medical_billing.model;

import com.springboot.medical_billing.enums.RequestStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Entity
@Table(name = "document_request")
public class DocumentRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "patient_id")
    private Patient patient;

    @ManyToOne
    @JoinColumn(name = "provider_id")
    private HealthcareProvider healthcareProvider;

    @Column(name = "request_date")
    private LocalDate requestDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private RequestStatus status;

    // documents sent by the hospital for this request
    @Column(name = "invoice_file")
    private String invoiceFile;

    @Column(name = "medical_file")
    private String medicalFile;

    @Column(name = "sent_date")
    private LocalDateTime sentDate;
}
