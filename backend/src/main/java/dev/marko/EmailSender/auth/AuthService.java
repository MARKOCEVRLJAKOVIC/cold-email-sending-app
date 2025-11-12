package dev.marko.EmailSender.auth;

import dev.marko.EmailSender.dtos.ConfirmationResponse;
import dev.marko.EmailSender.dtos.LoginRequest;
import dev.marko.EmailSender.dtos.RegisterUserRequest;
import dev.marko.EmailSender.dtos.UserDto;
import dev.marko.EmailSender.entities.Role;
import dev.marko.EmailSender.entities.User;
import dev.marko.EmailSender.exception.*;
import dev.marko.EmailSender.mappers.UserMapper;
import dev.marko.EmailSender.repositories.UserRepository;
import dev.marko.EmailSender.repositories.VerificationTokenRepository;
import dev.marko.EmailSender.security.*;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final JwtConfig jwtConfig;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final VerificationTokenRepository verificationTokenRepository;
    private final NotificationEmailService notificationEmailService;
    private final CurrentUserProvider currentUserProvider;

    @Value("${app.url}")
    private String appUrl;


    public UserDto registerUser(RegisterUserRequest request){
        if(userRepository.existsByEmail(request.getEmail())) throw new UserAlreadyExist();


        var user = userMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(Role.USER);
        user.setEnabled(false);

        userRepository.save(user);

        String token = UUID.randomUUID().toString();

        sendVerificationEmail(user, token);

        return userMapper.toDto(user);
    }

    public ConfirmationResponse confirmEmail(String token){

        var verificationToken = verificationTokenRepository.findByToken(token).orElseThrow(TokenNotFoundException::new);

        if(verificationToken.getExpirationDate().isBefore(LocalDateTime.now())) {
            throw new TokenExpiredException();
        }

        User user = verificationToken.getUser();
        user.setEnabled(true);
        userRepository.save(user);
        verificationTokenRepository.delete(verificationToken);

        return new ConfirmationResponse(true, "Email is successfully confirmed, you can now sign in.");

    }

    public JwtResponse login(LoginRequest request, HttpServletResponse response){

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        var user = userRepository.findByEmail(request.getEmail()).orElseThrow(UserNotFoundException::new);

        if(!user.getEnabled()){
            throw new UserNotConfirmedException();
        }
        var accessToken = jwtService.generateToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);

        var cookie = new Cookie("refreshToken", refreshToken.toString());
        cookie.setHttpOnly(true);
        cookie.setPath("/auth/refresh");
        cookie.setMaxAge(jwtConfig.getRefreshTokenExpiration());
        cookie.setSecure(true);
        response.addCookie(cookie);

        return new JwtResponse(accessToken.toString());
    }

    public JwtResponse refreshAccessToken(String refreshToken){

        var jwt = jwtService.parseToken(refreshToken);

        if(jwt == null || jwt.isExpired()){
            throw new JwtException("Token is expired");
        }

        var user = userRepository.findById(jwt.getUserId()).orElseThrow();

        var accessToken = jwtService.generateToken(user);

        return new JwtResponse(accessToken.toString());

    }


    public UserDto me(){

        var user = currentUserProvider.getCurrentUser();

        if(user == null) throw new UserNotFoundException();

        return userMapper.toDto(user);
    }

    private void sendVerificationEmail(User user, String token){

        VerificationToken verificationToken = VerificationToken.builder()
                .token(token)
                .user(user)
                .expirationDate(LocalDateTime.now().plusHours(24))
                .build();

        verificationTokenRepository.save(verificationToken);


        String confirmationUrl = appUrl + "/auth/confirm?token=" + token;

        notificationEmailService.sendEmail(
                user.getEmail(),
                "Verification of email address",
                notificationEmailService.confirmationMessage() + confirmationUrl
        );
    }

}
