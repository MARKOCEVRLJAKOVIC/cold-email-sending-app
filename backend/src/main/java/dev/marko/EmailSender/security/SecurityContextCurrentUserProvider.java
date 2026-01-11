package dev.marko.EmailSender.security;

import dev.marko.EmailSender.entities.User;
import dev.marko.EmailSender.exception.InvalidPrincipalException;
import dev.marko.EmailSender.exception.UnauthorizedGenericException;
import dev.marko.EmailSender.repositories.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@AllArgsConstructor
@Component
public class SecurityContextCurrentUserProvider implements CurrentUserProvider {

    private final UserRepository userRepository;


    @Override
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedGenericException("Unauthorized");
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof UserDetailsImpl userDetails) {
            return userDetails.user();
        }

        throw new InvalidPrincipalException("Invalid authentication principal");
    }


}
