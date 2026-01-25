package dev.marko.EmailSender.controllers;


import dev.marko.EmailSender.auth.AuthService;
import dev.marko.EmailSender.auth.NotificationEmailService;
import dev.marko.EmailSender.dtos.RegisterUserRequest;
import dev.marko.EmailSender.dtos.UserDto;
import dev.marko.EmailSender.entities.Role;
import dev.marko.EmailSender.exception.UserAlreadyExist;
import dev.marko.EmailSender.exception.UserNotFoundException;
import dev.marko.EmailSender.mappers.UserMapper;
import dev.marko.EmailSender.repositories.UserRepository;
import dev.marko.EmailSender.repositories.VerificationTokenRepository;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

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
    public ResponseEntity<UserDto> getUser(@PathVariable @Min(1) Long id){

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
    public ResponseEntity<UserDto> updateUser(@PathVariable @Min(1) Long id, @RequestBody RegisterUserRequest request, UriComponentsBuilder builder){

        var user = userRepository.findById(id).orElseThrow(UserNotFoundException::new);

        userMapper.update(request, user);

        userRepository.save(user);

        var userDto = userMapper.toDto(user);

        var uri = builder.path("users/{id}").buildAndExpand(user.getId()).toUri();

        return ResponseEntity.created(uri).body(userDto);

    }

    @DeleteMapping("/{id}")
    public ResponseEntity<UserDto> deleteUser(@PathVariable @Min(1) Long id){

        var user = userRepository.findById(id).orElseThrow(UserNotFoundException::new);

        userRepository.delete(user);

        return ResponseEntity.accepted().build();

    }

}