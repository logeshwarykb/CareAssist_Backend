package com.springboot.medical_billing.controller;

import com.springboot.medical_billing.dto.request.TpaDto;
import com.springboot.medical_billing.dto.request.TpaUpdateDto;
import com.springboot.medical_billing.dto.response.TpaResponseDto;
import com.springboot.medical_billing.service.TpaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class TpaController {

    private final TpaService tpaService;

    @PostMapping("/api/tpa/add")
    public void insertTpa(@Valid @RequestBody TpaDto tpaDto) {
        tpaService.insertTpa(tpaDto);
    }

    @GetMapping("/api/tpa/profile")
    public TpaResponseDto getProfile(Principal principal) {
        return tpaService.getByUsername(principal.getName());
    }

    @GetMapping("/api/tpa/{id}")
    public TpaResponseDto getById(@PathVariable Integer id) {
        return tpaService.getById(id);
    }

    @GetMapping("/api/tpa/all")
    public List<TpaResponseDto> getAll(
            @RequestParam(name = "page", defaultValue = "0") Integer page,
            @RequestParam(name = "size", defaultValue = "10") Integer size) {
        return tpaService.getAll(page, size);
    }

    @PutMapping("/api/tpa/update/{id}")
    public TpaResponseDto updateTpa(
            @PathVariable Integer id,
            @Valid @RequestBody TpaUpdateDto tpaDto) {
        return tpaService.updateTpa(id, tpaDto);
    }
}
