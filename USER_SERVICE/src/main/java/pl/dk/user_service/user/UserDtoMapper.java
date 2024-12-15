package pl.dk.user_service.user;

import pl.dk.user_service.user.dto.SaveUserDto;
import pl.dk.user_service.user.dto.UserDto;

class UserDtoMapper {

    public static User map(SaveUserDto saveUserDto) {
        return User.builder()
                .firstName(saveUserDto.firstName())
                .lastName(saveUserDto.lastName())
                .email(saveUserDto.email())
                .dateOfBirth(saveUserDto.dateOfBirth())
                .password(saveUserDto.password())
                .phone(saveUserDto.phone())
                .build();
    }

    public static UserDto map(User user) {
        return UserDto.builder()
                .userId(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .build();

    }

    public static SaveUserDto mapUserForUpdate(User user) {
        return SaveUserDto.builder()
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .dateOfBirth(user.getDateOfBirth())
                .password(user.getPassword())
                .phone(user.getPhone())
                .build();
    }

}
