package com.springboot.medical_billing.service;

import com.springboot.medical_billing.dto.request.InsuranceExecutiveDto;
import com.springboot.medical_billing.dto.request.InsuranceExecutiveUpdateDto;
import com.springboot.medical_billing.dto.response.InsuranceExecutiveResponseDto;
import com.springboot.medical_billing.enums.Role;
import com.springboot.medical_billing.exception.ResourceNotFoundException;
import com.springboot.medical_billing.model.InsuranceExecutive;
import com.springboot.medical_billing.model.User;
import com.springboot.medical_billing.repository.InsuranceExecutiveRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // Turns on Mockito: creates @Mock objects and injects them
public class InsuranceExecutiveServiceTest {

    @InjectMocks
    private InsuranceExecutiveService executiveService; // The REAL service being tested
    @Mock
    private InsuranceExecutiveRepository executiveRepository;
    @Mock
    private UserService userService;
    @Mock
    private PasswordEncoder passwordEncoder;

    // helper to prepare an insurance executive who is linked to a login user
    private InsuranceExecutive getExecutive(Integer id, String username) {
        InsuranceExecutive executive = new InsuranceExecutive();
        executive.setId(id);
        executive.setName("Star Health");
        executive.setContactNumber("9876543210");
        executive.setEmail("claims@starhealth.com");
        executive.setUser(new User(username, "encodedPassword", Role.INSURANCE_COMPANY));
        return executive;
    }

    // new insurance company registers, its login is created and the password is encrypted
    @Test
    public void insertExecutiveTest() {
        InsuranceExecutiveDto executiveDto = new InsuranceExecutiveDto("starhealth", "star@123",
                "Star Health", "9876543210", "claims@starhealth.com");
        User user = new User("starhealth", "star@123", Role.INSURANCE_COMPANY);

        when(userService.getUserObj("starhealth", "star@123", Role.INSURANCE_COMPANY)).thenReturn(user);
        when(passwordEncoder.encode("star@123")).thenReturn("encodedPassword");
        when(executiveRepository.save(any(InsuranceExecutive.class))).thenReturn(new InsuranceExecutive());

        executiveService.insertExecutive(executiveDto);

        assertEquals("encodedPassword", user.getPassword());

        verify(userService, times(1)).getUserObj("starhealth", "star@123", Role.INSURANCE_COMPANY);
        verify(passwordEncoder, times(1)).encode("star@123");
        verify(executiveRepository, times(1)).save(any(InsuranceExecutive.class));
        verifyNoMoreInteractions(userService);
        verifyNoMoreInteractions(executiveRepository);
    }

    // executive id 1 exists and the result has the correct name and username
    @Test
    public void getByIdTest() {
        when(executiveRepository.findById(1)).thenReturn(Optional.of(getExecutive(1, "starhealth")));

        InsuranceExecutiveResponseDto result = executiveService.getById(1);

        assertEquals("Star Health", result.name());
        assertEquals("starhealth", result.username());

        verify(executiveRepository, times(1)).findById(1);
        verifyNoMoreInteractions(executiveRepository);
    }

    // executive id 7 doesn't exist (invalid id)
    @Test
    public void getByIdTest_Invalid() {
        when(executiveRepository.findById(7)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> executiveService.getById(7));

        verify(executiveRepository, times(1)).findById(7);
        verifyNoMoreInteractions(executiveRepository);
    }

    // fetching all insurance companies: 2 in DB, so 2 results come back
    @Test
    public void getAllTest() {
          when(executiveRepository.findAll(PageRequest.of(0, 5)))
                .thenReturn(new PageImpl<>(List.of(getExecutive(1, "starhealth"),
                        getExecutive(2, "hdfcergo"))));

        assertEquals(2, executiveService.getAll(0, 5).size());

        verify(executiveRepository, times(1)).findAll(PageRequest.of(0, 5));
        verifyNoMoreInteractions(executiveRepository);
    }

    // executive id 1 exists, so name and email are updated
    @Test
    public void updateExecutiveTest() {
        InsuranceExecutive executive = getExecutive(1, "starhealth");
        InsuranceExecutiveUpdateDto executiveDto = new InsuranceExecutiveUpdateDto(
                "Star Health Insurance", "9000000000", "support@starhealth.com");

        when(executiveRepository.findById(1)).thenReturn(Optional.of(executive));
        when(executiveRepository.save(executive)).thenReturn(executive);

        InsuranceExecutiveResponseDto result = executiveService.updateExecutive(1, executiveDto);

        assertEquals("Star Health Insurance", result.name());
        assertEquals("support@starhealth.com", result.email());

        verify(executiveRepository, times(1)).findById(1);
        verify(executiveRepository, times(1)).save(executive);
        verifyNoMoreInteractions(executiveRepository);
    }

    // executive id 7 doesn't exist (invalid id), so nothing is updated
    @Test
    public void updateExecutiveTest_Invalid() {
        InsuranceExecutiveUpdateDto executiveDto = new InsuranceExecutiveUpdateDto(
                "Star Health Insurance", "9000000000", "support@starhealth.com");

        when(executiveRepository.findById(7)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> executiveService.updateExecutive(7, executiveDto));

        verify(executiveRepository, times(1)).findById(7);
        verify(executiveRepository, times(0)).save(any(InsuranceExecutive.class));
        verifyNoMoreInteractions(executiveRepository);
    }

    // logged-in insurance company "starhealth" fetches its own profile
    @Test
    public void getByUsernameTest() {
        when(executiveRepository.getByUsername("starhealth"))
                .thenReturn(Optional.of(getExecutive(1, "starhealth")));

        assertEquals("Star Health", executiveService.getByUsername("starhealth").name());

        verify(executiveRepository, times(1)).getByUsername("starhealth");
        verifyNoMoreInteractions(executiveRepository);
    }
}
