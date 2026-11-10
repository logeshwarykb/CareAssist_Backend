package com.springboot.medical_billing.service;

import com.springboot.medical_billing.dto.request.PatientDto;
import com.springboot.medical_billing.dto.request.PatientUpdateDto;
import com.springboot.medical_billing.dto.response.PatientResponseDto;
import com.springboot.medical_billing.enums.Gender;
import com.springboot.medical_billing.enums.Role;
import com.springboot.medical_billing.exception.OwnershipException;
import com.springboot.medical_billing.exception.ResourceNotFoundException;
import com.springboot.medical_billing.model.Patient;
import com.springboot.medical_billing.model.User;
import com.springboot.medical_billing.repository.PatientRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PatientServiceTest {

    @InjectMocks
    private PatientService patientService; // The REAL service being tested; Mockito puts the mocks below into it
    @Mock
    private PatientRepository patientRepository;
    @Mock
    private UserService userService;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private NotificationService notificationService;
    // new patient registers, his login is created and the password is encrypted
    @Test
    public void insertPatientTest() {
        PatientDto patientDto = new PatientDto("john", "john@123", "John Doe",
                LocalDate.of(2000, 1, 1), Gender.MALE, "9876543210", "Fever and cold");

        User user = new User("john", "john@123", Role.PATIENT);

        when(userService.getUserObj("john", "john@123", Role.PATIENT)).thenReturn(user);
        when(passwordEncoder.encode("john@123")).thenReturn("encodedPassword");
        when(patientRepository.save(any(Patient.class))).thenReturn(new Patient());

        patientService.insertPatient(patientDto);

        assertEquals("encodedPassword", user.getPassword());

        verify(userService, times(1)).getUserObj("john", "john@123", Role.PATIENT);
        verify(passwordEncoder, times(1)).encode("john@123");
        verify(patientRepository, times(1)).save(any(Patient.class));

        verifyNoMoreInteractions(userService);
        verifyNoMoreInteractions(passwordEncoder);
        verifyNoMoreInteractions(patientRepository);
    }

    // patient id 1 exists and the result has the correct name and username
    @Test
    public void getByIdTest() {
        User user = new User("john", "encodedPassword", Role.PATIENT);
        user.setId(10);

        Patient patient = new Patient();
        patient.setId(1);
        patient.setFullName("John Doe");
        patient.setGender(Gender.MALE);
        patient.setUser(user);

        when(patientRepository.findById(1)).thenReturn(Optional.of(patient));

        PatientResponseDto dto = patientService.getById(1);

        assertEquals("John Doe", dto.fullName());
        assertEquals("john", dto.username());

        verify(patientRepository, times(1)).findById(1);
        verifyNoMoreInteractions(patientRepository);
    }

    // patient id 7 doesn't exist (invalid id)
    @Test
    public void getByIdTest_Invalid() {
        when(patientRepository.findById(7)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> patientService.getById(7));

        verify(patientRepository, times(1)).findById(7);
        verifyNoMoreInteractions(patientRepository);
    }

    // "john" updates his own profile, so name and phone number are changed
    @Test
    public void updatePatientTest() {
        User user = new User("john", "encodedPassword", Role.PATIENT);
        Patient patient = new Patient();
        patient.setId(1);
        patient.setFullName("John Doe");
        patient.setUser(user);

        PatientUpdateDto updateDto = new PatientUpdateDto("John Updated",
                LocalDate.of(2000, 1, 1), Gender.MALE, "9999999999", "Recovered");

        when(patientRepository.findById(1)).thenReturn(Optional.of(patient));
        when(patientRepository.save(patient)).thenReturn(patient);

        PatientResponseDto dto = patientService.updatePatient(1, updateDto, "john");

        assertEquals("John Updated", dto.fullName());
        assertEquals("9999999999", dto.contactNumber());

        verify(patientRepository, times(1)).findById(1);
        verify(patientRepository, times(1)).save(patient);
        verifyNoMoreInteractions(patientRepository);
    }

    // "mary" tries to update john's profile (not her own), so it is blocked
    @Test
    public void updatePatientTest_Ownership() {
        User user = new User("john", "encodedPassword", Role.PATIENT);
        Patient patient = new Patient();
        patient.setId(1);
        patient.setUser(user);

        PatientUpdateDto updateDto = new PatientUpdateDto("Hacked Name",
                LocalDate.of(2000, 1, 1), Gender.MALE, "9999999999", null);

        when(patientRepository.findById(1)).thenReturn(Optional.of(patient));

        assertThrows(OwnershipException.class,
                () -> patientService.updatePatient(1, updateDto, "mary"));

        verify(patientRepository, times(1)).findById(1);
        verify(patientRepository, times(0)).save(any(Patient.class));
        verifyNoMoreInteractions(patientRepository);
    }
}