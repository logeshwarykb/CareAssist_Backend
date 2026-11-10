package com.springboot.medical_billing.controller;

import com.springboot.medical_billing.dto.request.AdministratorDto;
import com.springboot.medical_billing.dto.request.AdministratorUpdateDto;
import com.springboot.medical_billing.dto.response.AdministratorResponseDto;
import com.springboot.medical_billing.service.AdministratorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class AdministratorController {

    private final AdministratorService administratorService;

    @PostMapping("/api/administrator/add")
    public AdministratorResponseDto addAdministrator(
            @Valid @RequestBody AdministratorDto administratorDto) {
        return administratorService.addAdministrator(administratorDto);
    }

    @GetMapping("/api/administrator/profile")
    public AdministratorResponseDto getProfile(Principal principal) {
        return administratorService.getByUsername(principal.getName());
    }

    @PutMapping("/api/administrator/update/{id}")
    public AdministratorResponseDto updateAdministrator(
            @PathVariable Integer id,
            @Valid @RequestBody AdministratorUpdateDto dto) {
        return administratorService.updateAdministrator(id, dto);
    }

    @GetMapping("/api/administrator/{id}")
    public AdministratorResponseDto getAdministratorById(
            @PathVariable Integer id) {
        return administratorService.getAdministratorById(id);
    }

    @GetMapping("/api/administrator/all")
    public List<AdministratorResponseDto> getAllAdministrators(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return administratorService.getAllAdministrators(page, size);
    }

    @DeleteMapping("/api/administrator/{id}")
    public void deleteAdministrator(@PathVariable Integer id) {

        administratorService.deleteAdministrator(id);
    }
}