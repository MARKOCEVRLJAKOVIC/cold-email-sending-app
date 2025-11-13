package dev.marko.EmailSender.controllers;

import dev.marko.EmailSender.controllers.base.BaseController;
import dev.marko.EmailSender.dtos.RegisterEmailRequest;
import dev.marko.EmailSender.dtos.SmtpDto;
import dev.marko.EmailSender.services.SmtpService;
import dev.marko.EmailSender.services.base.BaseService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@RestController
@RequestMapping("/smtp")
public class SmtpController extends BaseController<SmtpDto, RegisterEmailRequest> {

    protected SmtpController(BaseService<?, SmtpDto, RegisterEmailRequest, ?> service) {
        super(service);
    }


//    @GetMapping
//    public ResponseEntity<List<SmtpDto>> getAllEmails(){
//
//        var smtpDtoList = smtpService.getAllSmtpCredentials();
//
//        return ResponseEntity.ok(smtpDtoList);
//
//    }
//
//    @GetMapping("/{id}")
//    public ResponseEntity<SmtpDto> getEmail(@PathVariable Long id){
//
//        var smtpDto = smtpService.getEmail(id);
//
//        return ResponseEntity.ok(smtpDto);
//
//    }
//
//    @PostMapping
//    public ResponseEntity<SmtpDto> registerEmail(
//            @Valid @RequestBody RegisterEmailRequest request,
//            UriComponentsBuilder builder){
//
//        var smtpDto = smtpService.registerEmail(request);
//
//        var uri = builder.path("/smtp/{id}").buildAndExpand(smtpDto.getId()).toUri();
//
//        return ResponseEntity.created(uri).body(smtpDto);
//
//    }
//
//    @PutMapping("/{id}")
//    public ResponseEntity<SmtpDto> updateEmail(
//            @PathVariable Long id,
//            @Valid @RequestBody RegisterEmailRequest request){
//
//        var smtpDto = smtpService.updateEmail(id, request);
//
//        return ResponseEntity.ok(smtpDto);
//
//    }
//
//    @DeleteMapping("{id}")
//    public ResponseEntity<Void> deleteEmail(@PathVariable Long id){
//
//        smtpService.deleteEmail(id);
//
//        return ResponseEntity.accepted().build();
//
//    }

}
