package com.springboot.medical_billing.repository;
import com.springboot.medical_billing.dto.response.DetailsDto;
import com.springboot.medical_billing.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    @Query("""
            select u
            from User u
            where u.username=?1 AND u.isActive = ?2
            """)
    User loadUserByUsername(String username, boolean isActive);

    @Query("""
            select u.username, u.isActive, u.role
            from User u
            where u.username=?1
            """)
    DetailsDto getUserDetails(String loggedInUsername);
}

