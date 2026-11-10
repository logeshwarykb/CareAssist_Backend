package com.springboot.medical_billing.config;

import com.springboot.medical_billing.service.MyUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final MyUserDetailsService myUserDetailsService;
    private final JwtFilter jwtFilter;

    @Bean
    public SecurityFilterChain defaultFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authorize -> authorize
                        // allow browser pre-check (CORS) requests
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/patient/add").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/patient/profile").hasAuthority("PATIENT")
                        .requestMatchers(HttpMethod.POST, "/api/provider/add").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/insurance/add").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/tpa/add").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/provider/profile").hasAuthority("HEALTHCARE_PROVIDER")
                        .requestMatchers(HttpMethod.GET, "/api/insurance/profile").hasAuthority("INSURANCE_COMPANY")
                        .requestMatchers(HttpMethod.POST, "/api/administrator/add").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/auth/login").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/auth/user-details").authenticated()
                        // ---- ADMINISTRATOR ----
                        .requestMatchers(HttpMethod.GET, "/api/patient/all").hasAuthority("ADMINISTRATOR")
                        .requestMatchers(HttpMethod.GET, "/api/administrator/all").hasAuthority("ADMINISTRATOR")
                        .requestMatchers(HttpMethod.DELETE, "/api/administrator/{id}").hasAuthority("ADMINISTRATOR")
                        // Admin dashboard - claims and payments
                        .requestMatchers(HttpMethod.GET, "/api/claim/all").hasAuthority("ADMINISTRATOR")
                        .requestMatchers(HttpMethod.GET, "/api/claim-payment/all").hasAuthority("ADMINISTRATOR")
                        .requestMatchers(HttpMethod.GET, "/api/provider/all").hasAnyAuthority("ADMINISTRATOR", "PATIENT")
                        .requestMatchers(HttpMethod.GET, "/api/insurance-plan/all").hasAuthority("ADMINISTRATOR")
                        .requestMatchers(HttpMethod.GET, "/api/administrator/{id}").hasAuthority("ADMINISTRATOR")
                        // Admin profile and edit
                        .requestMatchers(HttpMethod.GET, "/api/administrator/profile").hasAuthority("ADMINISTRATOR")
                        .requestMatchers(HttpMethod.PUT, "/api/administrator/update/{id}").hasAuthority("ADMINISTRATOR")
                        .requestMatchers(HttpMethod.GET, "/api/claim/status/stat").hasAuthority("ADMINISTRATOR")
                        // ---- PATIENT ----
                        .requestMatchers(HttpMethod.PUT, "/api/patient/update/{id}").hasAuthority("PATIENT")
                        .requestMatchers(HttpMethod.POST, "/api/patient-policy/add").hasAuthority("PATIENT")
                        .requestMatchers(HttpMethod.POST, "/api/claim/add").hasAuthority("PATIENT")
                        .requestMatchers(HttpMethod.GET, "/api/insurance-plan/filter").hasAuthority("PATIENT")
                        .requestMatchers(HttpMethod.GET, "/api/pre-auth/patient/v1/{patientId}").hasAuthority("PATIENT")
                        .requestMatchers(HttpMethod.GET, "/api/pre-auth/patient/v2").hasAuthority("PATIENT")
                        .requestMatchers(HttpMethod.GET, "/api/claim-payment/claim/{claimId}").hasAnyAuthority("PATIENT", "HEALTHCARE_PROVIDER")
                        .requestMatchers(HttpMethod.DELETE, "/api/claim/hard/{claimId}").hasAuthority("PATIENT")
                        .requestMatchers(HttpMethod.POST, "/api/claim/upload/{claimId}").hasAnyAuthority("PATIENT", "HEALTHCARE_PROVIDER")
                        // ---- HEALTHCARE PROVIDER ----
                        .requestMatchers(HttpMethod.PUT, "/api/provider/update/{id}").hasAuthority("HEALTHCARE_PROVIDER")
                        .requestMatchers(HttpMethod.POST, "/api/pre-auth/add").hasAuthority("HEALTHCARE_PROVIDER")
                        .requestMatchers(HttpMethod.PUT, "/api/claim/cashless/final-bill/{claimId}").hasAuthority("HEALTHCARE_PROVIDER")
                        .requestMatchers(HttpMethod.POST, "/api/patient/upload/{patientId}").hasAuthority("HEALTHCARE_PROVIDER")
                        .requestMatchers(HttpMethod.GET, "/api/pre-auth/provider/{providerId}").hasAuthority("HEALTHCARE_PROVIDER")
                        .requestMatchers(HttpMethod.GET, "/api/claim/provider/{providerId}").hasAuthority("HEALTHCARE_PROVIDER")
                        // ---- TPA ----
                        .requestMatchers(HttpMethod.GET, "/api/tpa/profile").hasAuthority("TPA")
                        .requestMatchers(HttpMethod.PUT, "/api/tpa/update/{id}").hasAuthority("TPA")
                        .requestMatchers(HttpMethod.PUT, "/api/pre-auth/status/{id}").hasAuthority("TPA")
                        .requestMatchers(HttpMethod.PUT, "/api/claim/tpa/final-approval/{claimId}").hasAuthority("TPA")
                        .requestMatchers(HttpMethod.GET, "/api/pre-auth/tpa/{tpaId}").hasAuthority("TPA")
                        .requestMatchers(HttpMethod.GET, "/api/claim/tpa/{tpaId}").hasAuthority("TPA")
                        // ---- INSURANCE COMPANY ----
                        .requestMatchers(HttpMethod.POST, "/api/insurance-plan/add").hasAuthority("INSURANCE_COMPANY")
                        .requestMatchers(HttpMethod.POST, "/api/insurance-plan/batch-add").hasAuthority("INSURANCE_COMPANY")
                        .requestMatchers(HttpMethod.GET, "/api/insurance-plan/my-plans").hasAuthority("INSURANCE_COMPANY")
                        .requestMatchers(HttpMethod.PUT, "/api/insurance-plan/update/{id}").hasAuthority("INSURANCE_COMPANY")
                        .requestMatchers(HttpMethod.DELETE, "/api/insurance-plan/delete/{id}").hasAuthority("INSURANCE_COMPANY")
                        .requestMatchers(HttpMethod.PUT, "/api/claim/status/{claimId}").hasAuthority("INSURANCE_COMPANY")
                        .requestMatchers(HttpMethod.PUT, "/api/insurance/update/{id}").hasAuthority("INSURANCE_COMPANY")
                        .requestMatchers(HttpMethod.POST, "/api/claim-payment/add").hasAuthority("INSURANCE_COMPANY")
                        .requestMatchers(HttpMethod.GET, "/api/claim/filter").hasAnyAuthority("INSURANCE_COMPANY", "ADMINISTRATOR")
                        .requestMatchers(HttpMethod.GET, "/api/claim/executive/{executiveId}").hasAuthority("INSURANCE_COMPANY")
                        .requestMatchers(HttpMethod.GET, "/api/claim/patient/v1/{patientId}").hasAnyAuthority("INSURANCE_COMPANY", "PATIENT")
                        .requestMatchers(HttpMethod.GET, "/api/claim/patient/v2").hasAuthority("PATIENT")
                        .requestMatchers(HttpMethod.POST, "/api/document-request/add").hasAuthority("PATIENT")
                        .requestMatchers(HttpMethod.GET, "/api/document-request/patient").hasAuthority("PATIENT")
                        .requestMatchers(HttpMethod.GET, "/api/document-request/provider").hasAuthority("HEALTHCARE_PROVIDER")
                        .requestMatchers(HttpMethod.PUT, "/api/document-request/complete/{id}").hasAuthority("HEALTHCARE_PROVIDER")
                        .requestMatchers(HttpMethod.GET, "/api/notification/my").hasAnyAuthority("PATIENT", "HEALTHCARE_PROVIDER", "TPA", "INSURANCE_COMPANY")
                        .anyRequest().permitAll()
                );
        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        http.httpBasic(Customizer.withDefaults());
        return http.build();
    }

    @Bean
    public AuthenticationProvider getProvider(){
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider(myUserDetailsService);
        authenticationProvider.setPasswordEncoder(getEncoder());
        return authenticationProvider;
    }

    @Bean
    public PasswordEncoder getEncoder(){

        return new BCryptPasswordEncoder();
    }
}