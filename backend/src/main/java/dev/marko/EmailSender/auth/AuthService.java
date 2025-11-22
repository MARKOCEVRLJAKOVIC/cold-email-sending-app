package dev.marko.EmailSender.auth;

import dev.marko.EmailSender.dtos.ConfirmationResponse;
import dev.marko.EmailSender.dtos.LoginRequest;
import dev.marko.EmailSender.dtos.RegisterUserRequest;
import dev.marko.EmailSender.dtos.UserDto;
import dev.marko.EmailSender.entities.Role;
import dev.marko.EmailSender.exception.*;
import dev.marko.EmailSender.mappers.UserMapper;
import dev.marko.EmailSender.repositories.UserRepository;
import dev.marko.EmailSender.security.*;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final JwtConfig jwtConfig;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final CurrentUserProvider currentUserProvider;
    private final VerificationTokenService verificationTokenService;

    String refreshTokenPath = "/auth/refresh";


    public UserDto registerUser(RegisterUserRequest request){
        if(userRepository.existsByEmail(request.getEmail())) throw new UserAlreadyExist();


        var user = userMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(Role.USER);
        user.setEnabled(false);

        userRepository.save(user);

        verificationTokenService.sendVerificationEmail(user);

        return userMapper.toDto(user);
    }

    public ConfirmationResponse confirmEmail(String token){
        return verificationTokenService.confirmEmail(token);
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
        cookie.setPath(refreshTokenPath);
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


}
