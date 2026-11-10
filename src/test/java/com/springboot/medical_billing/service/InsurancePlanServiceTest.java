package com.springboot.medical_billing.service;

import com.springboot.medical_billing.dto.request.InsurancePlanDto;
import com.springboot.medical_billing.dto.response.InsurancePlanFilterDto;
import com.springboot.medical_billing.dto.response.InsurancePlanResponseDto;
import com.springboot.medical_billing.enums.Role;
import com.springboot.medical_billing.exception.OwnershipException;
import com.springboot.medical_billing.exception.ResourceNotFoundException;
import com.springboot.medical_billing.mapper.InsurancePlanMapper;
import com.springboot.medical_billing.model.InsuranceExecutive;
import com.springboot.medical_billing.model.InsurancePlan;
import com.springboot.medical_billing.model.User;
import com.springboot.medical_billing.repository.InsuranceExecutiveRepository;
import com.springboot.medical_billing.repository.InsurancePlanRepository;
import com.springboot.medical_billing.repository.PatientPolicyRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // Turns on Mockito: creates @Mock objects and injects them
public class InsurancePlanServiceTest {

    @InjectMocks
    private InsurancePlanService insurancePlanService; // The REAL service being tested
    @Mock
    private InsurancePlanRepository insurancePlanRepository;
    @Mock
    private InsurancePlanMapper insurancePlanMapper;
    @Mock
    private InsuranceExecutiveRepository executiveRepository;
    @Mock
    private PatientPolicyRepository patientPolicyRepository;

    // helper: insurance company "starhealth"
    private InsuranceExecutive getExecutive(String username) {
        InsuranceExecutive executive = new InsuranceExecutive();
        executive.setId(1);
        executive.setName("Star Health");
        executive.setUser(new User(username, "encodedPassword", Role.INSURANCE_COMPANY));
        return executive;
    }

    // helper: plan "Gold Health" created by the given company
    private InsurancePlan getPlan(InsuranceExecutive executive) {
        InsurancePlan plan = new InsurancePlan();
        plan.setId(1);
        plan.setName("Gold Health");
        plan.setExecutive(executive);
        return plan;
    }

    private InsurancePlanResponseDto getResponseDto(Integer id, String name) {
        return new InsurancePlanResponseDto(id, name, 500000.0, 12000.0,
                "Hospitalisation and day care", "30 days waiting period", 1, "Star Health");
    }

    private InsurancePlanDto getPlanDto(String name) {
        return new InsurancePlanDto(name, 500000.0, 12000.0,
                "Hospitalisation and day care", "30 days waiting period");
    }

    // no plan named "Gold Health" exists yet, so the new plan is saved for company "starhealth"
    @Test
    public void insertInsurancePlanTest() {
        InsurancePlanDto planDto = getPlanDto("Gold Health");
        InsuranceExecutive executive = getExecutive("starhealth");
        InsurancePlan plan = new InsurancePlan();

        when(executiveRepository.getByUsername("starhealth")).thenReturn(Optional.of(executive));
        when(insurancePlanRepository.findByName("Gold Health")).thenReturn(Optional.empty());
        when(insurancePlanMapper.convertDtoToEntity(planDto)).thenReturn(plan);
        when(insurancePlanRepository.save(plan)).thenReturn(plan);
        when(insurancePlanMapper.convertEntityToDto(plan)).thenReturn(getResponseDto(1, "Gold Health"));

        InsurancePlanResponseDto result = insurancePlanService.insertInsurancePlan(planDto, "starhealth");

        assertEquals("Gold Health", result.name());
        assertEquals(executive, plan.getExecutive());

        verify(executiveRepository, times(1)).getByUsername("starhealth");
        verify(insurancePlanRepository, times(1)).findByName("Gold Health");
        verify(insurancePlanRepository, times(1)).save(plan);
        verifyNoMoreInteractions(insurancePlanRepository);
    }

    // plan "Gold Health" already exists (duplicate name), so it is not saved
    @Test
    public void insertInsurancePlanTest_Duplicate() {
        InsurancePlanDto planDto = getPlanDto("Gold Health");

        when(executiveRepository.getByUsername("starhealth")).thenReturn(Optional.of(getExecutive("starhealth")));
        when(insurancePlanRepository.findByName("Gold Health"))
                .thenReturn(Optional.of(new InsurancePlan()));

        assertThrows(IllegalArgumentException.class,
                () -> insurancePlanService.insertInsurancePlan(planDto, "starhealth"));

        verify(insurancePlanRepository, times(1)).findByName("Gold Health");
        verify(insurancePlanRepository, times(0)).save(any(InsurancePlan.class));
        verifyNoMoreInteractions(insurancePlanRepository);
        verifyNoMoreInteractions(insurancePlanMapper);
    }

    // plan id 1 exists and the result has the correct name
    @Test
    public void getByIdTest() {
        InsurancePlan plan = getPlan(getExecutive("starhealth"));

        when(insurancePlanRepository.findById(1)).thenReturn(Optional.of(plan));
        when(insurancePlanMapper.convertEntityToDto(plan)).thenReturn(getResponseDto(1, "Gold Health"));

        assertEquals("Gold Health", insurancePlanService.getById(1).name());

        verify(insurancePlanRepository, times(1)).findById(1);
        verify(insurancePlanMapper, times(1)).convertEntityToDto(plan);
        verifyNoMoreInteractions(insurancePlanRepository);
    }

    // plan id 7 doesn't exist (invalid id)
    @Test
    public void getByIdTest_Invalid() {
        when(insurancePlanRepository.findById(7)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> insurancePlanService.getById(7));

        verify(insurancePlanRepository, times(1)).findById(7);
        verifyNoMoreInteractions(insurancePlanRepository);
        verifyNoMoreInteractions(insurancePlanMapper);
    }

    // company "starhealth" searches "gold" -> 1 plan on page 0, pagination info is returned
    @Test
    public void searchPlansTest() {
        InsurancePlan plan = getPlan(getExecutive("starhealth"));

        when(insurancePlanRepository.searchPlans("starhealth", "gold", PageRequest.of(0, 5)))
                .thenReturn(new PageImpl<>(List.of(plan), PageRequest.of(0, 5), 1));
        when(insurancePlanMapper.convertEntityToDto(plan)).thenReturn(getResponseDto(1, "Gold Health"));

        InsurancePlanFilterDto result = insurancePlanService.searchPlans("starhealth", "gold", 0, 5);

        assertEquals(1, result.list().size());
        assertEquals(1, result.paginationInfo().totalPages());
        assertEquals(1, result.paginationInfo().totalRecords());

        verify(insurancePlanRepository, times(1)).searchPlans("starhealth", "gold", PageRequest.of(0, 5));
        verifyNoMoreInteractions(insurancePlanRepository);
    }

    // company "starhealth" edits its own plan, so name is updated
    @Test
    public void updateInsurancePlanTest() {
        InsurancePlan plan = getPlan(getExecutive("starhealth"));
        InsurancePlanDto planDto = getPlanDto("Gold Health Plus");

        when(insurancePlanRepository.findById(1)).thenReturn(Optional.of(plan));
        when(insurancePlanRepository.save(plan)).thenReturn(plan);
        when(insurancePlanMapper.convertEntityToDto(plan)).thenReturn(getResponseDto(1, "Gold Health Plus"));

        InsurancePlanResponseDto result = insurancePlanService.updateInsurancePlan(1, planDto, "starhealth");

        assertEquals("Gold Health Plus", plan.getName());
        assertEquals("Gold Health Plus", result.name());

        verify(insurancePlanRepository, times(1)).findById(1);
        verify(insurancePlanRepository, times(1)).save(plan);
    }

    // another company "lichealth" tries to edit starhealth's plan, so it is blocked
    @Test
    public void updateInsurancePlanTest_Ownership() {
        InsurancePlan plan = getPlan(getExecutive("starhealth"));

        when(insurancePlanRepository.findById(1)).thenReturn(Optional.of(plan));

        assertThrows(OwnershipException.class,
                () -> insurancePlanService.updateInsurancePlan(1, getPlanDto("New Name"), "lichealth"));

        verify(insurancePlanRepository, times(1)).findById(1);
        verify(insurancePlanRepository, times(0)).save(any(InsurancePlan.class));
        verifyNoMoreInteractions(insurancePlanRepository);
    }

    // plan not selected by any patient, so company "starhealth" can delete it
    @Test
    public void deleteInsurancePlanTest() {
        InsurancePlan plan = getPlan(getExecutive("starhealth"));

        when(insurancePlanRepository.findById(1)).thenReturn(Optional.of(plan));
        when(patientPolicyRepository.isPlanSelected(1)).thenReturn(false);

        insurancePlanService.deleteInsurancePlan(1, "starhealth");

        verify(insurancePlanRepository, times(1)).findById(1);
        verify(patientPolicyRepository, times(1)).isPlanSelected(1);
        verify(insurancePlanRepository, times(1)).delete(plan);
        verifyNoMoreInteractions(insurancePlanRepository);
    }

    // plan is already selected by patients, so it cannot be deleted
    @Test
    public void deleteInsurancePlanTest_Selected() {
        InsurancePlan plan = getPlan(getExecutive("starhealth"));

        when(insurancePlanRepository.findById(1)).thenReturn(Optional.of(plan));
        when(patientPolicyRepository.isPlanSelected(1)).thenReturn(true);

        assertThrows(IllegalArgumentException.class,
                () -> insurancePlanService.deleteInsurancePlan(1, "starhealth"));

        verify(insurancePlanRepository, times(1)).findById(1);
        verify(insurancePlanRepository, times(0)).delete(any(InsurancePlan.class));
        verifyNoMoreInteractions(insurancePlanRepository);
    }

    // filter plans with coverage between 2,00,000 and 6,00,000, so 2 plans come back with pagination info
    @Test
    public void filterInsurancePlansTest() {
        InsurancePlanResponseDto dto1 = getResponseDto(1, "Gold Health");
        InsurancePlanResponseDto dto2 = getResponseDto(2, "Silver Health");

        when(insurancePlanRepository.filterInsurancePlans(null, 200000.0, 600000.0, null, null,
                PageRequest.of(0, 5))).thenReturn(new PageImpl<>(List.of(dto1, dto2), PageRequest.of(0, 5), 2));

        InsurancePlanFilterDto result = insurancePlanService
                .filterInsurancePlans(null, 200000.0, 600000.0, null, null, 0, 5);

        assertEquals(2, result.list().size());
        assertEquals(1, result.paginationInfo().totalPages());
        assertEquals(2, result.paginationInfo().totalRecords());

        verify(insurancePlanRepository, times(1)).filterInsurancePlans(null, 200000.0, 600000.0,
                null, null, PageRequest.of(0, 5));
        verifyNoMoreInteractions(insurancePlanRepository);
    }

    // min coverage is greater than max coverage (invalid filter), so DB is never searched
    @Test
    public void filterInsurancePlansTest_Invalid() {
        // min coverage is greater than max coverage
        assertThrows(IllegalArgumentException.class,
                () -> insurancePlanService.filterInsurancePlans(null, 600000.0, 200000.0,
                        null, null, 0, 5));

        verifyNoMoreInteractions(insurancePlanRepository);
    }

    // company "starhealth" adds 2 new plans at once, so both are saved with saveAll
    @Test
    public void batchInsertInsurancePlansTest() {
        InsuranceExecutive executive = getExecutive("starhealth");
        InsurancePlanDto dto1 = getPlanDto("Gold Health");
        InsurancePlanDto dto2 = getPlanDto("Silver Health");
        InsurancePlan plan1 = new InsurancePlan();
        InsurancePlan plan2 = new InsurancePlan();

        when(executiveRepository.getByUsername("starhealth")).thenReturn(Optional.of(executive));
        when(insurancePlanRepository.findByName("Gold Health")).thenReturn(Optional.empty());
        when(insurancePlanRepository.findByName("Silver Health")).thenReturn(Optional.empty());
        when(insurancePlanMapper.convertDtoToEntity(dto1)).thenReturn(plan1);
        when(insurancePlanMapper.convertDtoToEntity(dto2)).thenReturn(plan2);

        insurancePlanService.batchInsertInsurancePlans(List.of(dto1, dto2), "starhealth");

        // company attached to every plan
        assertEquals(executive, plan1.getExecutive());
        assertEquals(executive, plan2.getExecutive());

        verify(executiveRepository, times(1)).getByUsername("starhealth");
        verify(insurancePlanRepository, times(1)).saveAll(List.of(plan1, plan2));
    }

    // second plan name already exists, so nothing is saved
    @Test
    public void batchInsertInsurancePlansTest_DuplicateName() {
        InsuranceExecutive executive = getExecutive("starhealth");
        InsurancePlanDto dto1 = getPlanDto("Gold Health");
        InsurancePlanDto dto2 = getPlanDto("Silver Health");

        when(executiveRepository.getByUsername("starhealth")).thenReturn(Optional.of(executive));
        when(insurancePlanRepository.findByName("Gold Health")).thenReturn(Optional.empty());
        when(insurancePlanRepository.findByName("Silver Health")).thenReturn(Optional.of(new InsurancePlan()));
        when(insurancePlanMapper.convertDtoToEntity(dto1)).thenReturn(new InsurancePlan());

        assertThrows(IllegalArgumentException.class,
                () -> insurancePlanService.batchInsertInsurancePlans(List.of(dto1, dto2), "starhealth"));

        verify(insurancePlanRepository, times(0)).saveAll(anyList());
    }
}
