package com.springboot.medical_billing.controller;

import com.springboot.medical_billing.dto.request.DocumentRequestDto;
import com.springboot.medical_billing.dto.response.DocumentRequestResponseDto;
import com.springboot.medical_billing.service.DocumentRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class DocumentRequestController {

    private final DocumentRequestService documentRequestService;

    // patient raises a request to the hospital
    @PostMapping("/api/document-request/add")
    public DocumentRequestResponseDto addRequest(@Valid @RequestBody DocumentRequestDto dto, Principal principal) {
        return documentRequestService.addRequest(dto, principal.getName());
    }

    // logged-in patient's requests
    @GetMapping("/api/document-request/patient")
    public List<DocumentRequestResponseDto> getPatientRequests(Principal principal) {
        return documentRequestService.getPatientRequests(principal.getName());
    }

    // requests received by the logged-in hospital
    @GetMapping("/api/document-request/provider")
    public List<DocumentRequestResponseDto> getProviderRequests(Principal principal) {
        return documentRequestService.getProviderRequests(principal.getName());
    }

    // hospital marks the request as completed after sending the documents
    @PutMapping("/api/document-request/complete/{id}")
    public DocumentRequestResponseDto completeRequest(@PathVariable Integer id, Principal principal) {
        return documentRequestService.completeRequest(id, principal.getName());
    }
}
