package com.springboot.medical_billing.service;

import com.springboot.medical_billing.dto.request.PatientDto;
import com.springboot.medical_billing.dto.request.PatientUpdateDto;
import com.springboot.medical_billing.dto.response.PatientResponseDto;
import com.springboot.medical_billing.enums.Role;
import com.springboot.medical_billing.exception.OwnershipException;
import com.springboot.medical_billing.exception.ResourceNotFoundException;
import com.springboot.medical_billing.mapper.PatientMapper;
import com.springboot.medical_billing.model.Patient;
import com.springboot.medical_billing.model.User;
import com.springboot.medical_billing.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PatientService {

    private final PatientRepository patientRepository;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final NotificationService notificationService;

    @Transactional
    public void insertPatient(PatientDto patientDto) {

        // Prepare User object and save it in DB
        User user = userService.getUserObj(
                patientDto.username(),
                patientDto.password(),
                Role.PATIENT
        );
        // Encode the password
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // Map PatientDto to Patient entity
        Patient patient = PatientMapper.convertDtoToEntity(patientDto);

        // Attach User to Patient
        patient.setUser(user);
        patientRepository.save(patient);
    }

    public PatientResponseDto getById(Integer id) {
        Optional<Patient> optional = patientRepository.findById(id);
        if (optional.isEmpty())
            throw new ResourceNotFoundException("Patient not found");
        return PatientMapper.convertEntityToDto(optional.get());
    }

    public List<PatientResponseDto> getAllPatient(
            Integer page, Integer size) {

        Pageable pageable = PageRequest.of(page, size);
        return patientRepository.getAllPatient(pageable);
    }

    @Transactional
    public PatientResponseDto updatePatient(Integer id, PatientUpdateDto patientDto, String patientUsername) {

        Optional<Patient> optional = patientRepository.findById(id);

        if (optional.isEmpty())
            throw new ResourceNotFoundException("Patient not found");

        Patient patient = optional.get();

        // Does this patient profile belong to the logged-in patient?
        if(! (patient.getUser().getUsername().equals(patientUsername))){
            throw new OwnershipException("Operation not allowed! You can update only your profile");
        }

        patient.setFullName(patientDto.fullName());
        patient.setDateOfBirth(patientDto.dateOfBirth());
        patient.setGender(patientDto.gender());
        patient.setContactNumber(patientDto.contactNumber());
        patient.setMedicalHistory(patientDto.medicalHistory());

        Patient updatedPatient = patientRepository.save(patient);

        return PatientMapper.convertEntityToDto(updatedPatient);
    }

    public PatientResponseDto getByUsername(String username) {
        Patient patient = patientRepository.getByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Patient profile not found"));
        return PatientMapper.convertEntityToDto(patient);
    }

    Logger logger = LoggerFactory.getLogger(PatientService.class);
    private final static String uploadDir = "C:/Users/loges/OneDrive/Desktop/careAssist_Ui/careassist-ui/public/uploads/";
    // upload method: check file, log details, copy into uploads folder, return the saved file name
    private String saveFile(MultipartFile file) throws IOException {
        if(file == null || file.isEmpty())
            throw new NullPointerException("Please upload valid file");

        logger.info("uploaded file name {}", file.getOriginalFilename());
        logger.info("File size {}",file.getSize());
        String ext = Objects.requireNonNull(file.getOriginalFilename()).split("\\.")[1];
        logger.info("File extension {}",  ext);

        // Original file name
        String originalFileName = System.currentTimeMillis() + "_" + file.getOriginalFilename() ;

        // file must stay inside the uploads folder (blocks names like ../../file.pdf)
        Path uploadPath = Paths.get(uploadDir).normalize();
        Path filePath = uploadPath.resolve(originalFileName).normalize();
        if (!filePath.startsWith(uploadPath)) {
            throw new IllegalArgumentException("Invalid file name");
        }

        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        return originalFileName;
    }

    // REIMBURSEMENT: hospital sends invoice + medical document to the patient (by patient ID)
    public void upload(Integer patientId, MultipartFile invoice, MultipartFile medical) throws IOException {
        // fetch patient
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found"));

        // Save filenames in the DB
        patient.setInvoiceFile(saveFile(invoice));
        patient.setMedicalFile(saveFile(medical));

        patientRepository.save(patient);

        // notify patient
        notificationService.add(patient.getUser(),
                "Hospital has sent your invoice and medical documents. You can submit your claim now");
    }

}