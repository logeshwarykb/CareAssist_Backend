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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // Turns on Mockito: creates @Mock objects and injects them
public class AdministratorServiceTest {

    @InjectMocks
    private AdministratorService administratorService; // The REAL service being tested
    @Mock
    private AdministratorRepository administratorRepository; // fake repository (no database is touched)
    @Mock
    private UserService userService;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AdministratorMapper administratorMapper;

    @Test
    public void addAdministratorTest() {
        // adds an admin
        AdministratorDto administratorDto = new AdministratorDto("admin", "admin@123", "Admin One");
        User user = new User("admin", "admin@123", Role.ADMINISTRATOR);
        Administrator administrator = new Administrator();
        AdministratorResponseDto responseDto = new AdministratorResponseDto(1, 10, "Admin One");

        when(userService.getUserObj("admin", "admin@123", Role.ADMINISTRATOR)).thenReturn(user);
        when(passwordEncoder.encode("admin@123")).thenReturn("encodedPassword");
        when(administratorMapper.convertDtoToEntity(administratorDto)).thenReturn(administrator);
        when(administratorRepository.save(administrator)).thenReturn(administrator);
        when(administratorMapper.convertEntityToDto(administrator)).thenReturn(responseDto);

        AdministratorResponseDto result = administratorService.addAdministrator(administratorDto);

        // password encoded and user attached to administrator
        assertEquals("encodedPassword", user.getPassword());
        assertEquals(user, administrator.getUser());
        assertEquals("Admin One", result.name());

        verify(userService, times(1)).getUserObj("admin", "admin@123", Role.ADMINISTRATOR);
        verify(passwordEncoder, times(1)).encode("admin@123");
        verify(administratorRepository, times(1)).save(administrator);
        verifyNoMoreInteractions(userService);
        verifyNoMoreInteractions(administratorRepository);
    }

    // id 1 exists and the result has the correct name
    @Test
    public void getAdministratorByIdTest() {
        Administrator administrator = new Administrator();
        administrator.setId(1);

        when(administratorRepository.findById(1)).thenReturn(Optional.of(administrator));
        when(administratorMapper.convertEntityToDto(administrator))
                .thenReturn(new AdministratorResponseDto(1, 10, "Admin One"));

        assertEquals("Admin One", administratorService.getAdministratorById(1).name());

        verify(administratorRepository, times(1)).findById(1);
        verifyNoMoreInteractions(administratorRepository);
    }

    // id 7 doesn’t exist(invalid id)
    @Test
    public void getAdministratorByIdTest_Invalid() {
        when(administratorRepository.findById(7)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> administratorService.getAdministratorById(7));

        verify(administratorRepository, times(1)).findById(7);
        verifyNoMoreInteractions(administratorRepository);
        verifyNoMoreInteractions(administratorMapper);
    }

    // deleting valid id
    @Test
    public void deleteAdministratorTest() {
        when(administratorRepository.existsById(1)).thenReturn(true);

        administratorService.deleteAdministrator(1);

        verify(administratorRepository, times(1)).existsById(1);
        verify(administratorRepository, times(1)).deleteById(1);
        verifyNoMoreInteractions(administratorRepository);
    }

    // trying to delete invalid id
    @Test
    public void deleteAdministratorTest_Invalid() {
        when(administratorRepository.existsById(7)).thenReturn(false);

        assertThrows(EntityNotFoundException.class,
                () -> administratorService.deleteAdministrator(7));

        verify(administratorRepository, times(1)).existsById(7);
        // delete was NEVER called
        verify(administratorRepository, times(0)).deleteById(7);
        verifyNoMoreInteractions(administratorRepository);
    }

    // admin 1 changes the name, so the new name is saved
    @Test
    public void updateAdministratorTest() {
        Administrator administrator = new Administrator();
        administrator.setId(1);
        administrator.setName("Admin One");

        when(administratorRepository.findById(1)).thenReturn(Optional.of(administrator));
        when(administratorRepository.save(administrator)).thenReturn(administrator);
        when(administratorMapper.convertEntityToDto(administrator))
                .thenReturn(new AdministratorResponseDto(1, 10, "Admin Two"));

        AdministratorResponseDto result = administratorService.updateAdministrator(1, new AdministratorUpdateDto("Admin Two"));

        assertEquals("Admin Two", administrator.getName());
        assertEquals("Admin Two", result.name());

        verify(administratorRepository, times(1)).findById(1);
        verify(administratorRepository, times(1)).save(administrator);
        verifyNoMoreInteractions(administratorRepository);
    }
}