package pl.dk.user_service.user;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


import pl.dk.user_service.user.repositoryDto.EmailPhoneDto;

@Repository
interface UserRepository extends JpaRepository<User, String>{

    Optional<EmailPhoneDto> findByEmailOrPhone(String email, String phone);
    
}