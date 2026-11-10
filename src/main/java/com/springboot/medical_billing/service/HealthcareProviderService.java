package com.springboot.medical_billing.service;

import com.springboot.medical_billing.dto.request.HealthcareProviderDto;
import com.springboot.medical_billing.dto.request.HealthcareProviderUpdateDto;
import com.springboot.medical_billing.dto.response.HealthcareProviderResponseDto;
import com.springboot.medical_billing.enums.Role;
import com.springboot.medical_billing.exception.ResourceNotFoundException;
import com.springboot.medical_billing.mapper.HealthcareProviderMapper;
import com.springboot.medical_billing.model.HealthcareProvider;
import com.springboot.medical_billing.model.User;
import com.springboot.medical_billing.repository.HealthcareProviderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class HealthcareProviderService {

    private final HealthcareProviderRepository providerRepository;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public HealthcareProviderResponseDto insertProvider(HealthcareProviderDto providerDto) {
        User user = userService.getUserObj(
                providerDto.username(),
                providerDto.password(),
                Role.HEALTHCARE_PROVIDER
        );
        // Encode the password
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        HealthcareProvider provider = HealthcareProviderMapper.convertDtoToEntity(providerDto);
        provider.setUser(user);
        HealthcareProvider savedProvider = providerRepository.save(provider);
        return HealthcareProviderMapper.convertEntityToDto(savedProvider);
    }

    public HealthcareProviderResponseDto getById(Integer id) {
        Optional<HealthcareProvider> optional =
                providerRepository.findById(id);
        if (optional.isEmpty())
            throw new ResourceNotFoundException(
                    "Healthcare provider not found");
        return HealthcareProviderMapper.convertEntityToDto(
                optional.get());
    }

    public List<HealthcareProviderResponseDto> getAll() {
        return providerRepository.findAll()
                .stream()
                .map(HealthcareProviderMapper::convertEntityToDto)
                .toList();
    }
    @Transactional
    public HealthcareProviderResponseDto updateProvider(
            Integer id, HealthcareProviderUpdateDto providerDto) {

        Optional<HealthcareProvider> optional =
                providerRepository.findById(id);

        if (optional.isEmpty()) {
            throw new ResourceNotFoundException(
                    "Healthcare provider not found");
        }

        HealthcareProvider provider = optional.get();

        provider.setName(providerDto.name());
        provider.setProviderType(providerDto.providerType());
        provider.setSpecialization(providerDto.specialization());
        provider.setContactNumber(providerDto.contactNumber());
        provider.setAddress(providerDto.address());

        HealthcareProvider updatedProvider = providerRepository.save(provider);

        return HealthcareProviderMapper.convertEntityToDto(updatedProvider);
    }

    public HealthcareProviderResponseDto getByUsername(String username) {
        HealthcareProvider provider = providerRepository.getByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Healthcare provider profile not found"));
        return HealthcareProviderMapper.convertEntityToDto(provider);
    }
}
