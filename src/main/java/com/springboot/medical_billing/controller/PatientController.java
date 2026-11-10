package com.springboot.medical_billing.controller;

import com.springboot.medical_billing.dto.request.PatientDto;
import com.springboot.medical_billing.dto.request.PatientUpdateDto;
import com.springboot.medical_billing.dto.response.PatientResponseDto;
import com.springboot.medical_billing.service.PatientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;
import java.util.List;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class PatientController {

    private final PatientService patientService;

    @PostMapping("/api/patient/add")
    public void insertPatient(@Valid @RequestBody PatientDto patientDto) {
        patientService.insertPatient(patientDto);
    }

    @GetMapping("/api/patient/{id}")
    public PatientResponseDto getById(@PathVariable Integer id) {
        return patientService.getById(id);
    }

    @GetMapping("/api/patient/all")
    public List<PatientResponseDto> getAllPatient( @RequestParam(name = "page", defaultValue = "0") int page,
                                                @RequestParam(name = "size", defaultValue = "10") int size) {
        return patientService.getAllPatient(page,size);
    }

    @PutMapping("/api/patient/update/{id}")
    public PatientResponseDto updatePatient(@PathVariable Integer id, @Valid @RequestBody PatientUpdateDto patientDto,
                                            Principal principal) {
        return patientService.updatePatient(id, patientDto, principal.getName());
    }

    @GetMapping("/api/patient/profile")
    public PatientResponseDto getProfile(Principal principal) {
        return patientService.getByUsername(principal.getName());
    }

    @PostMapping("/api/patient/upload/{patientId}")
    public void upload(@PathVariable Integer patientId,
                       @RequestParam("invoice") MultipartFile invoice,
                       @RequestParam("medical") MultipartFile medical) throws IOException {
        patientService.upload(patientId, invoice, medical);
    }

}