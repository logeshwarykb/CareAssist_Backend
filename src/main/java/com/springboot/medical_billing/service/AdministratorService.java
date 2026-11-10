package com.springboot.medical_billing.service;

import com.springboot.medical_billing.dto.request.AdministratorDto;
import com.springboot.medical_billing.dto.request.AdministratorUpdateDto;
import com.springboot.medical_billing.dto.response.AdministratorResponseDto;
import com.springboot.medical_billing.enums.Role;
import com.springboot.medical_billing.mapper.AdministratorMapper;
import com.springboot.medical_billing.model.Administrator;
import com.springboot.medical_billing.model.User;
import com.springboot.medical_billing.repository.AdministratorRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdministratorService {

    private final AdministratorRepository administratorRepository;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final AdministratorMapper administratorMapper;

    @Transactional
    public AdministratorResponseDto addAdministrator(AdministratorDto administratorDto) {

        // Step 1: Prepare User object and save it in DB
        User user = userService.getUserObj(
                administratorDto.username(),
                administratorDto.password(),
                Role.ADMINISTRATOR
        );
        // Step 1.5: Encode the password
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // Step 2: Map AdministratorDto to Administrator entity
        Administrator administrator =
                administratorMapper.convertDtoToEntity(administratorDto);

        // Step 3: Attach User to Administrator
        administrator.setUser(user);

        // Step 4: Save Administrator
        Administrator savedAdministrator = administratorRepository.save(administrator);

        return administratorMapper.convertEntityToDto(savedAdministrator);
    }

    public AdministratorResponseDto getAdministratorById(Integer id) {

        Administrator administrator = administratorRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Administrator ID not found"));

        return administratorMapper.convertEntityToDto(administrator);
    }

    public List<AdministratorResponseDto> getAllAdministrators(int page, int size) {

        Pageable pageable = PageRequest.of(page, size);

        return administratorRepository.findAll(pageable)
                .stream()
                .map(administratorMapper::convertEntityToDto)
                .toList();
    }

    public void deleteAdministrator(Integer id) {

        if (!administratorRepository.existsById(id)) {
            throw new EntityNotFoundException("Administrator ID not found");
        }

        administratorRepository.deleteById(id);
    }

    // logged-in admin's profile
    public AdministratorResponseDto getByUsername(String username) {
        Administrator administrator = administratorRepository.getByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("Administrator profile not found"));
        return administratorMapper.convertEntityToDto(administrator);
    }

    public AdministratorResponseDto updateAdministrator(Integer id, AdministratorUpdateDto dto) {
        // Step 1: Fetch admin
        Administrator administrator = administratorRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Administrator ID not found"));

        // Step 2: Update and save
        administrator.setName(dto.name());
        return administratorMapper.convertEntityToDto(administratorRepository.save(administrator));
    }
}