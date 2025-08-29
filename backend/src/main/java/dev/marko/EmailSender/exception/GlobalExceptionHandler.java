package dev.marko.EmailSender.exception;

import dev.marko.EmailSender.auth.TokenExpiredException;
import dev.marko.EmailSender.auth.TokenNotFoundException;
import dev.marko.EmailSender.dtos.ErrorDto;
import dev.marko.EmailSender.email.followup.FollowUpNotFoundException;
import dev.marko.EmailSender.email.gmailOAuth.GmailOAuthException;
import dev.marko.EmailSender.email.gmailOAuth.SmtpListIsEmptyException;
import dev.marko.EmailSender.email.reply.EmailReplyEmptyListException;
import dev.marko.EmailSender.email.reply.EmailReplyNotFoundException;
import dev.marko.EmailSender.email.send.EmailMessageNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getFieldErrors().forEach(error -> {
            String fieldName = error.getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        return ResponseEntity.badRequest().body(errors);
    }

    @ExceptionHandler(EmailMessageNotFoundException.class)
    public ResponseEntity<ErrorDto> handleEmailMessageNotFound() {
        return error(HttpStatus.NOT_FOUND, "Email message not found");

    }

    @ExceptionHandler(TemplateNotFoundException.class)
    public ResponseEntity<ErrorDto> handleTemplateNotFound() {
        return error(HttpStatus.NOT_FOUND, "Template not found");

    }

    @ExceptionHandler(EmailNotFoundException.class)
    public ResponseEntity<?> emailException(){
        return error(HttpStatus.NOT_FOUND, "Email account not found");
   }

    @ExceptionHandler(CampaignNotFoundException.class)
    public ResponseEntity<ErrorDto> handleCampaignException(){
        return error(HttpStatus.NOT_FOUND, "Campaign not found");
    }

    @ExceptionHandler(TemplateEmptyListException.class)
    public ResponseEntity<ErrorDto> handleTemplateEmptyListException(){
        return error(HttpStatus.NOT_FOUND, "List is empty");
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<?> userException(){
        return error(HttpStatus.NOT_FOUND, "User not found");
    }

    @ExceptionHandler(UserAlreadyExist.class)
    public ResponseEntity<?> userAlreadyExist(){
        return error(HttpStatus.BAD_REQUEST, "User already Exist");
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Void> handleBadCredentialsException(){
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }


    @ExceptionHandler(UserNotConfirmedException.class)
    public ResponseEntity<ErrorDto> handleUserConfirmationException(){
        return error(HttpStatus.BAD_REQUEST, "User is not confirmed");
    }


    @ExceptionHandler(TokenNotFoundException.class)
    public ResponseEntity<ErrorDto> handleTokenNotFoundException(){
        return error(HttpStatus.NOT_FOUND, "Token not found");
    }


    @ExceptionHandler(FollowUpNotFoundException.class)
    public ResponseEntity<ErrorDto> handleFollowUpNotFoundException(){
        return error(HttpStatus.NOT_FOUND, "Follow up message not found");

    }

    @ExceptionHandler(SmtpListIsEmptyException.class)
    public ResponseEntity<ErrorDto> handleSmtpEmptyListException(){
        return error(HttpStatus.NOT_FOUND, "List is empty");
    }

    @ExceptionHandler(EmailReplyNotFoundException.class)
    public ResponseEntity<ErrorDto> handleEmailReplyNotFoundException(){
        return error(HttpStatus.NOT_FOUND, "Email Reply not found");
    }

    @ExceptionHandler(EmailReplyEmptyListException.class)
    public ResponseEntity<ErrorDto> handleEmailReplyEmptyListException(){
        return error(HttpStatus.NOT_FOUND, "There are no email replies yer");
    }

    @ExceptionHandler(TokenExpiredException.class)
    public ResponseEntity<ErrorDto> handleTokenExpiredException(){
        return error(HttpStatus.NOT_FOUND, "Token has expired");
    }

    @ExceptionHandler(GmailOAuthException.class)
    public ResponseEntity<ErrorDto> handleGmailOAuthException(){
        return error(HttpStatus.NOT_FOUND, "Gmail account not found");
    }


    private ResponseEntity<ErrorDto> error(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(new ErrorDto(message));
    }

}
