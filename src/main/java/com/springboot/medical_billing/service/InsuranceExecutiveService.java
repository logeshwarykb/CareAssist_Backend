package com.springboot.medical_billing.service;

import com.springboot.medical_billing.dto.request.InsuranceExecutiveDto;
import com.springboot.medical_billing.dto.request.InsuranceExecutiveUpdateDto;
import com.springboot.medical_billing.dto.response.InsuranceExecutiveResponseDto;
import com.springboot.medical_billing.enums.Role;
import com.springboot.medical_billing.exception.ResourceNotFoundException;
import com.springboot.medical_billing.mapper.InsuranceExecutiveMapper;
import com.springboot.medical_billing.model.InsuranceExecutive;
import com.springboot.medical_billing.model.User;
import com.springboot.medical_billing.repository.InsuranceExecutiveRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class InsuranceExecutiveService {

    private final InsuranceExecutiveRepository executiveRepository;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void insertExecutive(InsuranceExecutiveDto executiveDto) {

        // Prepare User object and save it in DB
        User user = userService.getUserObj(
                executiveDto.username(),
                executiveDto.password(),
                Role.INSURANCE_COMPANY
        );
        // Encode the password
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // Map InsuranceExecutiveDto to InsuranceExecutive entity
        InsuranceExecutive executive =
                InsuranceExecutiveMapper.convertDtoToEntity(executiveDto);

        // Attach User to InsuranceExecutive
        executive.setUser(user);

        // Save InsuranceExecutive
        executiveRepository.save(executive);
    }

    public InsuranceExecutiveResponseDto getById(Integer id) {
        Optional<InsuranceExecutive> optional =
                executiveRepository.findById(id);
        if (optional.isEmpty())
            throw new ResourceNotFoundException("Insurance executive not found");
        return InsuranceExecutiveMapper.convertEntityToDto(
                optional.get());
    }

    public List<InsuranceExecutiveResponseDto> getAll(Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page, size);
        return executiveRepository.findAll(pageable)
                .stream()
                .map(InsuranceExecutiveMapper::convertEntityToDto)
                .toList();
    }

    @Transactional
    public InsuranceExecutiveResponseDto updateExecutive(
            Integer id, InsuranceExecutiveUpdateDto executiveDto) {

        Optional<InsuranceExecutive> optional =
                executiveRepository.findById(id);

        if (optional.isEmpty())
            throw new ResourceNotFoundException(
                    "Insurance executive not found");

        InsuranceExecutive executive = optional.get();

        executive.setName(executiveDto.name());
        executive.setContactNumber(executiveDto.contactNumber());
        executive.setEmail(executiveDto.email());

        return InsuranceExecutiveMapper.convertEntityToDto(
                executiveRepository.save(executive));
    }

    public InsuranceExecutiveResponseDto getByUsername(String username) {
        InsuranceExecutive executive = executiveRepository.getByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Insurance company profile not found"));
        return InsuranceExecutiveMapper.convertEntityToDto(executive);
    }
}
