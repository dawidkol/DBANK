package pl.dk.user_service.user;

import com.github.fge.jsonpatch.mergepatch.JsonMergePatch;

import pl.dk.user_service.user.dto.SaveUserDto;

import pl.dk.user_service.user.dto.UserDto;

interface UserService {

    UserDto registerUser(SaveUserDto saveUserDto);
    UserDto getUserById(String userId);
    void deleteUserById(String userId);
    void updateUser(String userId, JsonMergePatch jsonMergePatch);
    
}
