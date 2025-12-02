package dev.marko.EmailSender.auth;

import dev.marko.EmailSender.dtos.*;
import dev.marko.EmailSender.security.JwtResponse;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

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
    ) throws IOException {
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


}
