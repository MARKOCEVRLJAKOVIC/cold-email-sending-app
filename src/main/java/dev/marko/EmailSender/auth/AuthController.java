package dev.marko.EmailSender.auth;

import dev.marko.EmailSender.dtos.*;
import dev.marko.EmailSender.exception.UserAlreadyExist;
import dev.marko.EmailSender.exception.UserNotConfirmedException;
import dev.marko.EmailSender.exception.UserNotFoundException;
import dev.marko.EmailSender.security.JwtResponse;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;

@AllArgsConstructor
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("register")
    public ResponseEntity<UserDto> registerUser(
            @RequestBody RegisterUserRequest request){

        var userDto = authService.registerUser(request);

        return ResponseEntity.ok(userDto);

    }

    @GetMapping("/confirm")
    public ResponseEntity<ConfirmationResponse> confirmEmail(@RequestParam("token") String token){

        var confirmationResponse = authService.confirmEmail(token);

        return ResponseEntity.ok(confirmationResponse);


    }


    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletResponse response
    ){
        return ResponseEntity.ok(authService.login(request, response));
    }

    @PostMapping("/refresh")
    public ResponseEntity<JwtResponse> refreshAccessToken(
            @CookieValue(value = "refreshToken") String refreshToken){
        return ResponseEntity.ok(authService.refreshAccessToken(refreshToken));
    }

    @PostMapping("/me")
    public ResponseEntity<UserDto> me(){

        var userDto = authService.me();
        return ResponseEntity.ok(userDto);

    }


    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Void> handleBadCredentialsException(){
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorDto> handleUserException(){

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorDto("User not found"));
    }

    @ExceptionHandler(UserNotConfirmedException.class)
    public ResponseEntity<ErrorDto> handleUserConfirmationException(){
        return ResponseEntity.badRequest().body(new ErrorDto("User is not confirmed."));
    }


    @ExceptionHandler(UserAlreadyExist.class)
    public ResponseEntity<ErrorDto> handleUserAlreadyExist(){
        return ResponseEntity.badRequest().body(new ErrorDto("User already exist."));
    }

    @ExceptionHandler(TokenNotFoundException.class)
    public ResponseEntity<ErrorDto> handleTokenNotFoundException(){
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorDto("Token not found"));
    }


}
