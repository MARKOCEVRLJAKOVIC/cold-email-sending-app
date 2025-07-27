package dev.marko.EmailSender.controllers;


import dev.marko.EmailSender.dtos.ErrorDto;
import dev.marko.EmailSender.dtos.RegisterUserRequest;
import dev.marko.EmailSender.dtos.UserDto;
import dev.marko.EmailSender.entities.Role;
import dev.marko.EmailSender.exception.UserAlreadyExist;
import dev.marko.EmailSender.exception.UserNotFoundException;
import dev.marko.EmailSender.mappers.UserMapper;
import dev.marko.EmailSender.repositories.UserRepository;
import dev.marko.EmailSender.repositories.VerificationTokenRepository;
import dev.marko.EmailSender.auth.AuthService;
import dev.marko.EmailSender.auth.NotificationEmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/users")
public class UserController {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final VerificationTokenRepository verificationTokenRepository;
    private final NotificationEmailService notificationEmailService;
    private final AuthService authService;


    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUser(@PathVariable Long id){

        var user = userRepository.findById(id).orElseThrow(UserNotFoundException::new);

        var userDto = userMapper.toDto(user);

        return ResponseEntity.ok(userDto);
    }


    @PostMapping("registerAdmin")
    public ResponseEntity<UserDto> registerAdmin(@RequestBody RegisterUserRequest request){

        if(userRepository.existsByEmail(request.getEmail())) throw new UserAlreadyExist();

        var user = userMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(Role.ADMIN);

        userRepository.save(user);

        var userDto = userMapper.toDto(user);

        return ResponseEntity.ok(userDto);

    }


    @PutMapping("/{id}")
    public ResponseEntity<UserDto> updateUser(@PathVariable Long id, @RequestBody RegisterUserRequest request){

        var user = userRepository.findById(id).orElseThrow(UserNotFoundException::new);

        userMapper.update(request, user);

        userRepository.save(user);

        var userDto = userMapper.toDto(user);

        return ResponseEntity.ok(userDto);

    }

    @DeleteMapping("/{id}")
    public ResponseEntity<UserDto> deleteUser(@PathVariable Long id){

        var user = userRepository.findById(id).orElseThrow(UserNotFoundException::new);

        userRepository.delete(user);

        return ResponseEntity.accepted().build();

    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<?> userNotFound(){
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorDto("User not found"));
    }

    @ExceptionHandler(UserAlreadyExist.class)
    public ResponseEntity<?> userAlreadyExist(){
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorDto("User already exist"));
    }

}