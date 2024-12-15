package pl.dk.user_service.user;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.fge.jsonpatch.JsonPatchException;
import com.github.fge.jsonpatch.mergepatch.JsonMergePatch;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import pl.dk.user_service.exception.UserConstraintException;
import pl.dk.user_service.exception.UserNotFoundException;
import pl.dk.user_service.notification.NotificationService;
import pl.dk.user_service.user.dto.SaveUserDto;
import pl.dk.user_service.user.dto.UserDto;
import pl.dk.user_service.user.repositoryDto.EmailPhoneDto;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private NotificationService notificationService;
    private AutoCloseable autoCloseable;
    private ObjectMapper objectMapper;
    private UserService underTest;

    String userId;
    String email;
    String phone;
    String firstName;
    String lastName;
    String password;
    LocalDate dateOfBirth;

    SaveUserDto saveUserDto;
    User user;

    @BeforeEach
    void setUp() {
        autoCloseable = MockitoAnnotations.openMocks(this);
        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        underTest = new UserServiceImpl(userRepository, objectMapper, notificationService);

        userId = UUID.randomUUID().toString();
        email = "john.doe@test.pl";
        phone = "+48666999666";

        firstName = "John";
        lastName = "Doe";
        password = "securepassword123";
        dateOfBirth = LocalDate.of(1990, 1, 1);

        saveUserDto = SaveUserDto.builder()
                .firstName(firstName)
                .lastName(lastName)
                .email(email)
                .phone(phone)
                .password(password)
                .dateOfBirth(dateOfBirth)
                .build();

        user = User.builder()
                .id(userId)
                .firstName(firstName)
                .lastName(lastName)
                .email(email)
                .phone(phone)
                .password(password)
                .dateOfBirth(dateOfBirth)
                .build();
    }

    @AfterEach
    void tearDown() throws Exception {
        autoCloseable.close();
    }

    @Test
    @DisplayName("It should register User")
    void itShouldRegisterUser() {
        // Given
        when(userRepository.findByEmailOrPhone(email, phone)).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(user);
        doNothing().when(notificationService).sendToRegistrationTopic(any(UserDto.class));

        // When
        UserDto result = underTest.registerUser(saveUserDto);

        // Then
        verify(userRepository, times(1)).findByEmailOrPhone(email, phone);
        verify(userRepository, times(1)).save(any(User.class));
        assertAll(
                () -> assertThat(result.userId()).isNotBlank(),
                () -> assertThat(result.firstName()).isEqualTo(firstName),
                () -> assertThat(result.lastName()).isEqualTo(lastName),
                () -> assertThat(result.email()).isEqualTo(email)
        );
    }

    @Test
    @DisplayName("It should throw UserConstraintException when user try to register with existing phone and email")
    void itShouldThrowUserConstraintExceptionWhenUserTryToRegisterWithExistingPhoneAndEmail() {
        // Given
        EmailPhoneDto emailPhoneDto = EmailPhoneDto.builder()
                .phone(phone)
                .email(email).build();
        when(userRepository.findByEmailOrPhone(email, phone)).thenReturn(Optional.of(emailPhoneDto));

        // When
        UserConstraintException userConstraintException = assertThrows(UserConstraintException.class, () -> underTest.registerUser(saveUserDto));

        // Then
        verify(userRepository, times(1)).findByEmailOrPhone(email, phone);
        assertThat(userConstraintException.getMessage()).contains(email);
        assertThat(userConstraintException.getMessage()).contains(phone);
    }

    @Test
    @DisplayName("It should throw UserConstraintException when user try to register with existing phone")
    void itShouldThrowUserConstraintExceptionWhenUserTryToRegisterWithExistingPhone() {
        // Given
        String notExistingEmail = "notexistingemail@test.pl";

        EmailPhoneDto emailPhoneDto = EmailPhoneDto.builder()
                .phone(phone)
                .email(notExistingEmail)
                .build();

        when(userRepository.findByEmailOrPhone(email, phone)).thenReturn(Optional.of(emailPhoneDto));

        // When
        UserConstraintException userConstraintException = assertThrows(UserConstraintException.class, () -> underTest.registerUser(saveUserDto));

        // Then
        verify(userRepository, times(1)).findByEmailOrPhone(email, phone);
        assertThat(userConstraintException.getMessage()).contains(notExistingEmail);
    }

    @Test
    @DisplayName("It should throw UserConstraintException when user try to register with existing email")
    void itShouldThrowUserConstraintExceptionWhenUserTryToRegisterWithExistingEmail() {
        // Given
        String notExistingPhone = "+48999999999";

        EmailPhoneDto emailPhoneDto = EmailPhoneDto.builder()
                .phone(notExistingPhone)
                .email(email)
                .build();

        when(userRepository.findByEmailOrPhone(email, phone)).thenReturn(Optional.of(emailPhoneDto));

        // When
        UserConstraintException userConstraintException = assertThrows(UserConstraintException.class, () -> underTest.registerUser(saveUserDto));

        // Then
        verify(userRepository, times(1)).findByEmailOrPhone(email, phone);
        assertThat(userConstraintException.getMessage()).contains(notExistingPhone);
    }

    @Test
    @DisplayName("It should get user by given userId")
    void itShouldGetUserById() {
        // Given
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // When
        UserDto result = underTest.getUserById(userId);

        // Then
        verify(userRepository, times(1)).findById(userId);
        assertAll(
                () -> assertThat(result.userId()).isEqualTo(userId),
                () -> assertThat(result.firstName()).isEqualTo(firstName),
                () -> assertThat(result.lastName()).isEqualTo(lastName),
                () -> assertThat(result.email()).isEqualTo(email)
        );
    }

    @Test
    @DisplayName("It should delete user by given userId")
    void itShouldDeleteUserById() {
        // Given
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // When
        underTest.deleteUserById(userId);

        // Then
        ArgumentCaptor<User> userArgumentCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).delete(userArgumentCaptor.capture());
        User argumentCaptorValue = userArgumentCaptor.getValue();
        assertAll(
                () -> assertThat(argumentCaptorValue.getId()).isEqualTo(userId),
                () -> assertThat(argumentCaptorValue.getFirstName()).isEqualTo(firstName),
                () -> assertThat(argumentCaptorValue.getLastName()).isEqualTo(lastName),
                () -> assertThat(argumentCaptorValue.getEmail()).isEqualTo(email),
                () -> assertThat(argumentCaptorValue.getPassword()).isEqualTo(password),
                () -> assertThat(argumentCaptorValue.getPhone()).isEqualTo(phone),
                () -> assertThat(argumentCaptorValue.getDateOfBirth()).isEqualTo(dateOfBirth)
        );
    }

    @Test
    @DisplayName("It should throw UserNotFoundException when user by given userId not exists")
    void itShouldThrowUserNotFoundExceptionWhenUserByGivenUserIdNotExists() {
        // Given
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When
        // Then
        UserNotFoundException userNotFoundException = assertThrows(UserNotFoundException.class, () -> underTest.deleteUserById(userId));
        assertThat(userNotFoundException.getMessage()).contains(userId);
    }

    @Test
    @DisplayName("It should update user")
    void itShouldUpdateUser() throws JsonPatchException {
        // Given

        String updatedText = "updated";
        String updatedPhone = "+48666666666";
        LocalDate updatedDateOfBirth = dateOfBirth.minusYears(10);
        SaveUserDto updateUser = SaveUserDto.builder()
                .firstName(updatedText + firstName)
                .lastName(updatedText + lastName)
                .email(updatedText + email)
                .phone(updatedPhone)
                .password(updatedText + password)
                .dateOfBirth(updatedDateOfBirth)
                .build();

        JsonNode jsonNode = objectMapper.valueToTree(updateUser);
        JsonMergePatch jsonMergePatch = JsonMergePatch.fromJson(jsonNode);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // When
        underTest.updateUser(userId, jsonMergePatch);

        // Then
        verify(userRepository, times(1)).findById(userId);
        ArgumentCaptor<User> userArgumentCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userArgumentCaptor.capture());
        User argumentCaptorValue = userArgumentCaptor.getValue();

        assertAll(
                () -> assertThat(argumentCaptorValue.getId()).isEqualTo(userId),
                () -> assertThat(argumentCaptorValue.getFirstName()).isEqualTo(updateUser.firstName()),
                () -> assertThat(argumentCaptorValue.getLastName()).isEqualTo(updateUser.lastName()),
                () -> assertThat(argumentCaptorValue.getEmail()).isEqualTo(updateUser.email()),
                () -> assertThat(argumentCaptorValue.getPhone()).isEqualTo(updateUser.phone()),
                () -> assertThat(argumentCaptorValue.getPassword()).isEqualTo(updateUser.password()),
                () -> assertThat(argumentCaptorValue.getDateOfBirth()).isEqualTo(updateUser.dateOfBirth())
        );
    }
}