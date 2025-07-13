package dev.marko.EmailSender.controllers;

import dev.marko.EmailSender.dtos.ErrorDto;
import dev.marko.EmailSender.dtos.RegisterEmailRequest;
import dev.marko.EmailSender.dtos.SmtpDto;
import dev.marko.EmailSender.exception.EmailNotFoundException;
import dev.marko.EmailSender.exception.UserNotFoundException;
import dev.marko.EmailSender.mappers.SmtpMapper;
import dev.marko.EmailSender.repositories.SmtpRepository;
import dev.marko.EmailSender.repositories.UserRepository;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("/smtp")
public class SmtpController {

    private final SmtpRepository smtpRepository;
    private final SmtpMapper smtpMapper;
    private final UserRepository userRepository;

    @GetMapping("/{id}")
    public ResponseEntity<SmtpDto> getEmail(@PathVariable Long id){

        var smtp = smtpRepository.findById(id).orElseThrow(EmailNotFoundException::new);

        return ResponseEntity.ok(smtpMapper.toDto(smtp));

    }
    @GetMapping("/users/{userId}")
    public ResponseEntity<List<SmtpDto>> getEmailFromUser(@PathVariable Long userId){

        var user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);

        var smtpList = smtpRepository.findAllByUserId(userId);

        if(smtpList.isEmpty()){
            throw new EmailNotFoundException();
        }

        var smtpDtoList = smtpList.stream()
                .map(smtpMapper::toDto)
                .toList();

        return ResponseEntity.ok(smtpDtoList);

    }

    @PostMapping
    public ResponseEntity<SmtpDto> registerEmail(
            @Valid @RequestBody RegisterEmailRequest request,
            UriComponentsBuilder builder){

        var user = userRepository.findById(request.getUserId()).orElseThrow(UserNotFoundException::new);

        var smtp = smtpMapper.toEntity(request);
        smtp.setUser(user);

        smtp.setSmtpPort(request.getSmtpPort());

        smtpRepository.save(smtp);

        var smtpDto = smtpMapper.toDto(smtp);
        smtpDto.setUserId(user.getId());

        var uri = builder.path("/smtp/{id}").buildAndExpand(smtpDto.getId()).toUri();


        return ResponseEntity.created(uri).body(smtpDto);

    }

    @PutMapping("/{id}")
    public ResponseEntity<SmtpDto> updateEmail(
            @PathVariable Long id,
            @Valid @RequestBody RegisterEmailRequest request){

        var user = userRepository.findById(request.getUserId()).orElseThrow(UserNotFoundException::new);

        var smtp = smtpRepository.findById(request.getUserId()).orElseThrow(EmailNotFoundException::new);
        smtp.setUser(user);

        smtpMapper.update(request, smtp);

        smtpRepository.save(smtp);

        var smtpDto = smtpMapper.toDto(smtp);

        return ResponseEntity.ok(smtpDto);

    }

    @DeleteMapping("{id}")
    public ResponseEntity<Void> deleteEmail(@PathVariable Long id){

        var smtp = smtpRepository.findById(id).orElseThrow(EmailNotFoundException::new);

        smtpRepository.delete(smtp);

        return ResponseEntity.accepted().build();
    }

    @ExceptionHandler(EmailNotFoundException.class)
    public ResponseEntity<?> emailException(){
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorDto("Email not found"));
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<?> userException(){
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorDto("User not found"));
    }


}
