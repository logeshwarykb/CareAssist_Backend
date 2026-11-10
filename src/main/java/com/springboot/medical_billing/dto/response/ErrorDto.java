package com.springboot.medical_billing.dto.response;

import java.time.Instant;

public record ErrorDto(
        String message,
        String comments,
        Instant Timestamp  )
{

}
