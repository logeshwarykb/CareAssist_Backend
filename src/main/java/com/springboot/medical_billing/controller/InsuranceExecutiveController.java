package com.springboot.medical_billing.controller;

import com.springboot.medical_billing.dto.request.InsuranceExecutiveDto;
import com.springboot.medical_billing.dto.request.InsuranceExecutiveUpdateDto;
import com.springboot.medical_billing.dto.response.InsuranceExecutiveResponseDto;
import com.springboot.medical_billing.service.InsuranceExecutiveService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class InsuranceExecutiveController {

    private final InsuranceExecutiveService executiveService;

    @PostMapping("/api/insurance/add")
    public void insertExecutive(
            @Valid @RequestBody InsuranceExecutiveDto executiveDto) {
        executiveService.insertExecutive(executiveDto);
    }

    @GetMapping("/api/insurance/{id}")
    public InsuranceExecutiveResponseDto getById(
            @PathVariable Integer id) {
        return executiveService.getById(id);
    }

    @GetMapping("/api/insurance/all")
    public List<InsuranceExecutiveResponseDto> getAll(
            @RequestParam(name = "page", defaultValue = "0") Integer page,
            @RequestParam(name = "size", defaultValue = "10") Integer size) {
        return executiveService.getAll(page, size);
    }

    @PutMapping("/api/insurance/update/{id}")
    public InsuranceExecutiveResponseDto updateExecutive(
            @PathVariable Integer id,
            @Valid @RequestBody InsuranceExecutiveUpdateDto executiveDto) {

        return executiveService.updateExecutive(id, executiveDto);
    }

    @GetMapping("/api/insurance/profile")
    public InsuranceExecutiveResponseDto getProfile(Principal principal) {
        return executiveService.getByUsername(principal.getName());
    }
}
