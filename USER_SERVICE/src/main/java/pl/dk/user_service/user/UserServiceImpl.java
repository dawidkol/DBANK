package pl.dk.user_service.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatchException;
import com.github.fge.jsonpatch.mergepatch.JsonMergePatch;

import lombok.RequiredArgsConstructor;
import pl.dk.user_service.notification.NotificationService;
import pl.dk.user_service.user.dto.SaveUserDto;

import pl.dk.user_service.user.dto.UserDto;
import pl.dk.user_service.exception.UserConstraintException;
import pl.dk.user_service.exception.ServerException;
import pl.dk.user_service.exception.UserNotFoundException;
import pl.dk.user_service.user.repositoryDto.EmailPhoneDto;

@Service
@RequiredArgsConstructor
@Slf4j
class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;
    private final NotificationService notificationService;


    @Override
    @Transactional
    public UserDto registerUser(SaveUserDto saveUserDto) {
        String userEmail = saveUserDto.email();
        String phone = saveUserDto.phone();

        userRepository.findByEmailOrPhone(userEmail, phone).ifPresent(
                (emailPhone) -> {
                    String result = checkRegisterConditions(emailPhone, userEmail, phone);
                    throw new UserConstraintException(
                            "User with %s = %s already exists".formatted(result, emailPhone));
                });

        User userToSave = UserDtoMapper.map(saveUserDto);
        User savedUser = userRepository.save(userToSave);
        UserDto result = UserDtoMapper.map(savedUser);
        notificationService.sendToRegistrationTopic(result);
        return result;
    }

    private String checkRegisterConditions(EmailPhoneDto emailPhone, String userEmail, String phone) {
        String emailFromDB = emailPhone.email();
        String phoneFromDB = emailPhone.phone();
        boolean emailCondition = emailFromDB.equals(userEmail);
        boolean phoneCondition = phoneFromDB.equals(phone);

        String result = null;
        if (phoneCondition && emailCondition) {
            result = "email and phone";
        } else if (phoneCondition) {
            result = "phone";
        } else {
            result = "email";
        }
        return result;
    }

    @Override
    public UserDto getUserById(String userId) {
        return userRepository.findById(userId)
                .map(UserDtoMapper::map)
                .orElseThrow(() -> new UserNotFoundException("User with id = %s not found".formatted(userId)));
    }

    @Override
    @Transactional
    public void deleteUserById(String userId) {
        userRepository.findById(userId)
                .ifPresentOrElse(user -> {
                    user.setActive(false);
                }, () -> {
                    throw new UserNotFoundException("User with id = %s not found".formatted(userId));
                });
    }

    @Override
    @Transactional
    public void updateUser(String userId, JsonMergePatch jsonMergePatch) {
        SaveUserDto prepareData = userRepository.findById(userId)
                .map(UserDtoMapper::mapUserForUpdate)
                .orElseThrow(() -> new UserNotFoundException("User with id = %s not found".formatted(userId)));
        try {
            SaveUserDto patchedData = this.applyPatch(prepareData, jsonMergePatch);
            User userToUpdate = UserDtoMapper.map(patchedData);
            userToUpdate.setId(userId);
            userToUpdate.setActive(true);
            userRepository.save(userToUpdate);
        } catch (JsonProcessingException | JsonPatchException e) {
            throw new ServerException("Something goes wrong on application side, try again later");
        }

    }

    private SaveUserDto applyPatch(SaveUserDto saveUserDto, JsonMergePatch jsonMergePatch)
            throws JsonPatchException, JsonProcessingException {
        JsonNode jsonNode = objectMapper.valueToTree(saveUserDto);
        JsonNode jsonNodePatched = jsonMergePatch.apply(jsonNode);
        return objectMapper.treeToValue(jsonNodePatched, SaveUserDto.class);
    }

}
