package pl.dk.user_service.user.repositoryDto;

import lombok.Builder;

@Builder
public record EmailPhoneDto(String email, String phone) {
    
}
