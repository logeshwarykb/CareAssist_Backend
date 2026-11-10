package com.springboot.medical_billing.service;

import com.springboot.medical_billing.dto.response.DetailsDto;
import com.springboot.medical_billing.enums.Role;
import com.springboot.medical_billing.model.User;
import com.springboot.medical_billing.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public User getUserObj(String username, String password, Role role) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setRole(role);

        return userRepository.save(user);
    }

    public DetailsDto getUserDetails(String loggedInUsername) {
        return userRepository.getUserDetails(loggedInUsername);
    }
}