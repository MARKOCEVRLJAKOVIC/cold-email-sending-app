package dev.marko.EmailSender.exception;

import dev.marko.EmailSender.auth.TokenExpiredException;
import dev.marko.EmailSender.auth.TokenNotFoundException;
import dev.marko.EmailSender.dtos.ErrorDto;
import dev.marko.EmailSender.email.connection.gmailOAuth.GmailOAuthException;
import dev.marko.EmailSender.email.connection.gmailOAuth.SmtpListIsEmptyException;
import dev.marko.EmailSender.email.followup.FollowUpNotFoundException;
import dev.marko.EmailSender.email.reply.EmailReplyEmptyListException;
import dev.marko.EmailSender.email.reply.EmailReplyNotFoundException;
import dev.marko.EmailSender.email.send.EmailMessageNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {


    @ExceptionHandler({
            EmailMessageNotFoundException.class,
            TemplateNotFoundException.class,
            CampaignNotFoundException.class,
            FollowUpNotFoundException.class,
            UserNotFoundException.class,
            TokenNotFoundException.class,
            EmailReplyNotFoundException.class,
            EmailNotFoundException.class
    })
    public ResponseEntity<ErrorDto> handleNotFound(RuntimeException ex) {
        return error(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler({
            TemplateEmptyListException.class,
            EmailReplyEmptyListException.class,
            MissingRefreshTokenException.class
    })
    public ResponseEntity<ErrorDto> handleBadRequest(RuntimeException ex) {
        return error(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler({
            TokenExpiredException.class,
            UserNotConfirmedException.class
    })
    public ResponseEntity<ErrorDto> handleUnauthorized(RuntimeException ex) {
        return error(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    @ExceptionHandler(UserAlreadyExist.class)
    public ResponseEntity<ErrorDto> handleConflict(UserAlreadyExist ex) {
        return error(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler({
            GmailOAuthException.class,
            SmtpListIsEmptyException.class
    })
    public ResponseEntity<ErrorDto> handleServerError(RuntimeException ex) {
        return error(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(err ->
                errors.put(err.getField(), err.getDefaultMessage())
        );
        return ResponseEntity.badRequest().body(errors);
    }


    // helper

    private ResponseEntity<ErrorDto> error(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(new ErrorDto(message));
    }

}
