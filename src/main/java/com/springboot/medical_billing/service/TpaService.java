package com.springboot.medical_billing.service;

import com.springboot.medical_billing.dto.request.TpaDto;
import com.springboot.medical_billing.dto.request.TpaUpdateDto;
import com.springboot.medical_billing.dto.response.TpaResponseDto;
import com.springboot.medical_billing.enums.Role;
import com.springboot.medical_billing.exception.ResourceNotFoundException;
import com.springboot.medical_billing.mapper.TpaMapper;
import com.springboot.medical_billing.model.Tpa;
import com.springboot.medical_billing.model.User;
import com.springboot.medical_billing.repository.TpaRepository;
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
public class TpaService {

    private final TpaRepository tpaRepository;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void insertTpa(TpaDto tpaDto) {

        // Prepare User object and save it in DB
        User user = userService.getUserObj(
                tpaDto.username(),
                tpaDto.password(),
                Role.TPA
        );
        // Encode the password
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // Map TpaDto to Tpa entity
        Tpa tpa = TpaMapper.convertDtoToEntity(tpaDto);

        // Attach User to Tpa
        tpa.setUser(user);

        // Save Tpa
        tpaRepository.save(tpa);
    }

    public TpaResponseDto getById(Integer id) {
        Optional<Tpa> optional = tpaRepository.findById(id);
        if (optional.isEmpty())
            throw new ResourceNotFoundException("TPA not found");
        return TpaMapper.convertEntityToDto(optional.get());
    }

    public List<TpaResponseDto> getAll(Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page, size);
        return tpaRepository.findAll(pageable)
                .stream()
                .map(TpaMapper::convertEntityToDto)
                .toList();
    }

    @Transactional
    public TpaResponseDto updateTpa(Integer id, TpaUpdateDto tpaDto) {

        Optional<Tpa> optional = tpaRepository.findById(id);

        if (optional.isEmpty())
            throw new ResourceNotFoundException("TPA not found");

        Tpa tpa = optional.get();

        tpa.setName(tpaDto.name());
        tpa.setContactNumber(tpaDto.contactNumber());
        tpa.setEmail(tpaDto.email());

        return TpaMapper.convertEntityToDto(tpaRepository.save(tpa));
    }

    public TpaResponseDto getByUsername(String username) {
        Tpa tpa = tpaRepository.getByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("TPA profile not found"));
        return TpaMapper.convertEntityToDto(tpa);
    }
}
