package com.springboot.medical_billing.service;

import com.springboot.medical_billing.dto.request.TpaDto;
import com.springboot.medical_billing.dto.request.TpaUpdateDto;
import com.springboot.medical_billing.dto.response.TpaResponseDto;
import com.springboot.medical_billing.enums.Role;
import com.springboot.medical_billing.exception.ResourceNotFoundException;
import com.springboot.medical_billing.model.Tpa;
import com.springboot.medical_billing.model.User;
import com.springboot.medical_billing.repository.TpaRepository;
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
public class TpaServiceTest {

    @InjectMocks
    private TpaService tpaService; // The REAL service being tested
    @Mock
    private TpaRepository tpaRepository;
    @Mock
    private UserService userService;
    @Mock
    private PasswordEncoder passwordEncoder;

    // helper to prepare a TPA who is linked to a login user
    private Tpa getTpa(Integer id, String username) {
        Tpa tpa = new Tpa();
        tpa.setId(id);
        tpa.setName("Medi Assist");
        tpa.setContactNumber("9876543210");
        tpa.setEmail("support@mediassist.com");
        tpa.setUser(new User(username, "encodedPassword", Role.TPA));
        return tpa;
    }

    // new TPA registers, its login is created and the password is encrypted
    @Test
    public void insertTpaTest() {
        TpaDto tpaDto = new TpaDto("mediassist", "medi@123",
                "Medi Assist", "9876543210", "support@mediassist.com");
        User user = new User("mediassist", "medi@123", Role.TPA);

        when(userService.getUserObj("mediassist", "medi@123", Role.TPA)).thenReturn(user);
        when(passwordEncoder.encode("medi@123")).thenReturn("encodedPassword");
        when(tpaRepository.save(any(Tpa.class))).thenReturn(new Tpa());

        tpaService.insertTpa(tpaDto);

        assertEquals("encodedPassword", user.getPassword());

        verify(userService, times(1)).getUserObj("mediassist", "medi@123", Role.TPA);
        verify(passwordEncoder, times(1)).encode("medi@123");
        verify(tpaRepository, times(1)).save(any(Tpa.class));
        verifyNoMoreInteractions(userService);
        verifyNoMoreInteractions(tpaRepository);
    }

    // TPA id 1 exists and the result has the correct name and username
    @Test
    public void getByIdTest() {
        when(tpaRepository.findById(1)).thenReturn(Optional.of(getTpa(1, "mediassist")));

        TpaResponseDto result = tpaService.getById(1);

        assertEquals("Medi Assist", result.name());
        assertEquals("mediassist", result.username());

        verify(tpaRepository, times(1)).findById(1);
        verifyNoMoreInteractions(tpaRepository);
    }

    // TPA id 7 doesn't exist (invalid id)
    @Test
    public void getByIdTest_Invalid() {
        when(tpaRepository.findById(7)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> tpaService.getById(7));

        verify(tpaRepository, times(1)).findById(7);
        verifyNoMoreInteractions(tpaRepository);
    }

    // fetching all TPAs: 2 in DB, so 2 results come back
    @Test
    public void getAllTest() {
        when(tpaRepository.findAll(PageRequest.of(0, 5)))
                .thenReturn(new PageImpl<>(List.of(getTpa(1, "mediassist"),
                        getTpa(2, "paramount"))));

        assertEquals(2, tpaService.getAll(0, 5).size());

        verify(tpaRepository, times(1)).findAll(PageRequest.of(0, 5));
        verifyNoMoreInteractions(tpaRepository);
    }

    // TPA id 1 exists, so name and email are updated
    @Test
    public void updateTpaTest() {
        Tpa tpa = getTpa(1, "mediassist");
        TpaUpdateDto tpaDto = new TpaUpdateDto(
                "Medi Assist India", "9000000000", "help@mediassist.com");

        when(tpaRepository.findById(1)).thenReturn(Optional.of(tpa));
        when(tpaRepository.save(tpa)).thenReturn(tpa);

        TpaResponseDto result = tpaService.updateTpa(1, tpaDto);

        assertEquals("Medi Assist India", result.name());
        assertEquals("help@mediassist.com", result.email());

        verify(tpaRepository, times(1)).findById(1);
        verify(tpaRepository, times(1)).save(tpa);
        verifyNoMoreInteractions(tpaRepository);
    }

    // logged-in TPA "mediassist" fetches own profile
    @Test
    public void getByUsernameTest() {
        when(tpaRepository.getByUsername("mediassist")).thenReturn(Optional.of(getTpa(1, "mediassist")));

        assertEquals(1, tpaService.getByUsername("mediassist").id());

        verify(tpaRepository, times(1)).getByUsername("mediassist");
        verifyNoMoreInteractions(tpaRepository);
    }
}
