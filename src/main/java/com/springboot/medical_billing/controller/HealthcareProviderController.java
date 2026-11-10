package com.springboot.medical_billing.controller;

import com.springboot.medical_billing.dto.request.HealthcareProviderDto;
import com.springboot.medical_billing.dto.request.HealthcareProviderUpdateDto;
import com.springboot.medical_billing.dto.response.HealthcareProviderResponseDto;
import com.springboot.medical_billing.service.HealthcareProviderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class HealthcareProviderController {

    private final HealthcareProviderService providerService;

    @PostMapping("/api/provider/add")
    public HealthcareProviderResponseDto insertProvider(
            @Valid @RequestBody HealthcareProviderDto providerDto) {

        return providerService.insertProvider(providerDto);
    }

    @GetMapping("/api/provider/{id}")
    public HealthcareProviderResponseDto getById(
            @PathVariable Integer id) {

        return providerService.getById(id);
    }

    @GetMapping("/api/provider/all")
    public List<HealthcareProviderResponseDto> getAll() {
        return providerService.getAll();
    }
    @PutMapping("/api/provider/update/{id}")
    public HealthcareProviderResponseDto updateProvider(
            @PathVariable Integer id,
            @Valid @RequestBody HealthcareProviderUpdateDto providerDto) {

        return providerService.updateProvider(id, providerDto);
    }

    @GetMapping("/api/provider/profile")
    public HealthcareProviderResponseDto getProfile(Principal principal) {
        return providerService.getByUsername(principal.getName());
    }
}
