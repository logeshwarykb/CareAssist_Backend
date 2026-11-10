package com.springboot.medical_billing.mapper;

import com.springboot.medical_billing.dto.request.ClaimDto;
import com.springboot.medical_billing.dto.response.ChartDto;
import com.springboot.medical_billing.dto.response.ClaimResponseDto;
import com.springboot.medical_billing.dto.response.ClaimStatusCountDto;
import com.springboot.medical_billing.model.Claim;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ClaimMapper {

    public static Claim convertDtoToEntity(ClaimDto dto) {
        Claim claim = new Claim();
        claim.setClaimDate(dto.claimDate());
        claim.setClaimAmount(dto.claimAmount());
        return claim;
    }

    public static ClaimResponseDto convertEntityToDto(Claim claim){
        return
                new ClaimResponseDto(
                        claim.getId(),
                        0,
                        "",
                        0,
                        0,
                        "",
                        claim.getClaimDate(),
                        claim.getClaimAmount(),
                        claim.getStatus(),
                        claim.getClaimType(),
                        0,
                        "",
                        0,
                        claim.getApprovedAmount(),
                        "",
                        0,
                        "",
                        claim.getInvoiceFile(),
                        claim.getMedicalFile(),
                        claim.getApprovedDate()
                );
    }
    public static ChartDto convertToChartDto(List<ClaimStatusCountDto> list) {
        List<String> labels = list.stream().map(dto -> dto.status().toString()).toList();
        List<Long> values = list.stream().map(ClaimStatusCountDto::count).toList();
        return new ChartDto(labels, values);
    }

}
