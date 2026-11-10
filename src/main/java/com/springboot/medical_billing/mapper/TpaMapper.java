package com.springboot.medical_billing.mapper;

import com.springboot.medical_billing.dto.request.TpaDto;
import com.springboot.medical_billing.dto.response.TpaResponseDto;
import com.springboot.medical_billing.model.Tpa;

public class TpaMapper {

    public static Tpa convertDtoToEntity(TpaDto tpaDto) {
        Tpa tpa = new Tpa();
        tpa.setName(tpaDto.name());
        tpa.setContactNumber(tpaDto.contactNumber());
        tpa.setEmail(tpaDto.email());
        return tpa;
    }

    public static TpaResponseDto convertEntityToDto(Tpa tpa) {
        return new TpaResponseDto(
                tpa.getId(),
                tpa.getUser().getId(),
                tpa.getUser().getUsername(),
                tpa.getName(),
                tpa.getContactNumber(),
                tpa.getEmail()
        );
    }
}
