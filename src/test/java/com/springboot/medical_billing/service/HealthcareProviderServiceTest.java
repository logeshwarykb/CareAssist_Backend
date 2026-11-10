package com.springboot.medical_billing.service;

import com.springboot.medical_billing.dto.request.HealthcareProviderDto;
import com.springboot.medical_billing.dto.request.HealthcareProviderUpdateDto;
import com.springboot.medical_billing.dto.response.HealthcareProviderResponseDto;
import com.springboot.medical_billing.enums.ProviderType;
import com.springboot.medical_billing.enums.Role;
import com.springboot.medical_billing.enums.Specialization;
import com.springboot.medical_billing.exception.ResourceNotFoundException;
import com.springboot.medical_billing.model.HealthcareProvider;
import com.springboot.medical_billing.model.User;
import com.springboot.medical_billing.repository.HealthcareProviderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class HealthcareProviderServiceTest {

    @InjectMocks
    private HealthcareProviderService providerService;
    @Mock
    private HealthcareProviderRepository providerRepository;
    @Mock
    private UserService userService;
    @Mock
    private PasswordEncoder passwordEncoder;

    private HealthcareProvider getProvider(Integer id, String username) {
        HealthcareProvider provider = new HealthcareProvider();
        provider.setId(id);
        provider.setName("City Hospital");
        provider.setProviderType(ProviderType.HOSPITAL);
        provider.setSpecialization(Specialization.GENERAL);
        provider.setContactNumber("9876543210");
        provider.setAddress("Chennai");
        provider.setUser(new User(username, "encodedPassword", Role.HEALTHCARE_PROVIDER));
        return provider;
    }

    // new hospital registers, its login is created and the password is encrypted
    @Test
    public void insertProviderTest() {
        HealthcareProviderDto providerDto = new HealthcareProviderDto("cityhospital", "city@123",
                "City Hospital", ProviderType.HOSPITAL, Specialization.GENERAL,
                "9876543210", "Chennai");
        User user = new User("cityhospital", "city@123", Role.HEALTHCARE_PROVIDER);
        HealthcareProvider savedProvider = getProvider(1, "cityhospital");

        when(userService.getUserObj("cityhospital", "city@123", Role.HEALTHCARE_PROVIDER))
                .thenReturn(user);
        when(passwordEncoder.encode("city@123")).thenReturn("encodedPassword");
        // when save is called, don't go to DB, just give back a saved provider
        when(providerRepository.save(any(HealthcareProvider.class))).thenReturn(savedProvider);

        HealthcareProviderResponseDto result = providerService.insertProvider(providerDto);

        assertEquals("encodedPassword", user.getPassword());
        assertEquals("City Hospital", result.name());
        assertEquals("cityhospital", result.username());

        verify(userService, times(1)).getUserObj("cityhospital", "city@123", Role.HEALTHCARE_PROVIDER);
        verify(passwordEncoder, times(1)).encode("city@123");
        verify(providerRepository, times(1)).save(any(HealthcareProvider.class));
        verifyNoMoreInteractions(userService);
        verifyNoMoreInteractions(providerRepository);
    }

    // provider id 1 exists and the result has the correct provider type
    @Test
    public void getByIdTest() {
        when(providerRepository.findById(1)).thenReturn(Optional.of(getProvider(1, "cityhospital")));

        assertEquals(ProviderType.HOSPITAL, providerService.getById(1).providerType());

        verify(providerRepository, times(1)).findById(1);
        verifyNoMoreInteractions(providerRepository);
    }

    // provider id 7 doesn't exist (invalid id)
    @Test
    public void getByIdTest_Invalid() {
        when(providerRepository.findById(7)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> providerService.getById(7));

        verify(providerRepository, times(1)).findById(7);
        verifyNoMoreInteractions(providerRepository);
    }

    // fetching all providers: 2 providers in DB, so 2 results come back
    @Test
    public void getAllTest() {
        when(providerRepository.findAll())
                .thenReturn(List.of(getProvider(1, "cityhospital"), getProvider(2, "carelab")));

        assertEquals(2, providerService.getAll().size());

        verify(providerRepository, times(1)).findAll();
        verifyNoMoreInteractions(providerRepository);
    }

    // provider id 1 exists, so type, specialization and address are updated
    @Test
    public void updateProviderTest() {
        HealthcareProvider provider = getProvider(1, "cityhospital");
        HealthcareProviderUpdateDto providerDto = new HealthcareProviderUpdateDto(
                "City Hospital", ProviderType.CLINIC, Specialization.CARDIOLOGY,
                "9000000000", "Coimbatore");

        when(providerRepository.findById(1)).thenReturn(Optional.of(provider));
        when(providerRepository.save(provider)).thenReturn(provider);

        HealthcareProviderResponseDto result = providerService.updateProvider(1, providerDto);

        assertEquals(ProviderType.CLINIC, result.providerType());
        assertEquals(Specialization.CARDIOLOGY, result.specialization());
        assertEquals("Coimbatore", result.address());

        verify(providerRepository, times(1)).findById(1);
        verify(providerRepository, times(1)).save(provider);
        verifyNoMoreInteractions(providerRepository);
    }

    // provider id 7 doesn't exist (invalid id), so nothing is updated
    @Test
    public void updateProviderTest_Invalid() {
        HealthcareProviderUpdateDto providerDto = new HealthcareProviderUpdateDto(
                "City Hospital", ProviderType.CLINIC, Specialization.CARDIOLOGY,
                "9000000000", "Coimbatore");

        when(providerRepository.findById(7)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> providerService.updateProvider(7, providerDto));

        verify(providerRepository, times(1)).findById(7);
        verify(providerRepository, times(0)).save(any(HealthcareProvider.class));
        verifyNoMoreInteractions(providerRepository);
    }

    // logged-in hospital "cityhospital" fetches its own profile
    @Test
    public void getByUsernameTest() {
        when(providerRepository.getByUsername("cityhospital"))
                .thenReturn(Optional.of(getProvider(1, "cityhospital")));

        assertEquals(1, providerService.getByUsername("cityhospital").id());

        verify(providerRepository, times(1)).getByUsername("cityhospital");
        verifyNoMoreInteractions(providerRepository);
    }

    // no hospital with username "unknown", so profile is not found
    @Test
    public void getByUsernameTest_Invalid() {
        when(providerRepository.getByUsername("unknown")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> providerService.getByUsername("unknown"));

        verify(providerRepository, times(1)).getByUsername("unknown");
        verifyNoMoreInteractions(providerRepository);
    }
}
