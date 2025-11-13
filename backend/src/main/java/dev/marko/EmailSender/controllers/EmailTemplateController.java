package dev.marko.EmailSender.controllers;

import dev.marko.EmailSender.controllers.base.BaseController;
import dev.marko.EmailSender.dtos.CreateTemplateRequest;
import dev.marko.EmailSender.dtos.EmailTemplateDto;
import dev.marko.EmailSender.dtos.RegisterEmailRequest;
import dev.marko.EmailSender.dtos.SmtpDto;
import dev.marko.EmailSender.services.EmailTemplateService;
import dev.marko.EmailSender.services.base.BaseService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("templates")
public class EmailTemplateController extends BaseController<EmailTemplateDto, CreateTemplateRequest> {


    public EmailTemplateController(EmailTemplateService emailTemplateService) {
        super(emailTemplateService);
    }

//    @GetMapping
//    public ResponseEntity<List<EmailTemplateDto>> getAllTemplates(){
//
//       var emailListDto = emailTemplateService.getAll();
//
//       return ResponseEntity.ok(emailListDto);
//
//    }
//
//    @GetMapping("/{id}")
//    public ResponseEntity<EmailTemplateDto> getTemplate(
//            @PathVariable Long id){
//
//        var emailTemplateDto = emailTemplateService.get(id);
//        return ResponseEntity.ok(emailTemplateDto);
//
//    }
//
//    @PostMapping
//    public ResponseEntity<EmailTemplateDto> createTemplate(@RequestBody CreateTemplateRequest request){
//
//        var emailTemplateDto = emailTemplateService.createTemplate(request);
//
//        return ResponseEntity.ok(emailTemplateDto);
//
//    }
//
//    @PutMapping("/{id}")
//    public ResponseEntity<EmailTemplateDto> updateTemplate(
//            @PathVariable Long id,
//            @RequestBody CreateTemplateRequest request){
//
//        var emailTemplateDto = emailTemplateService.updateTemplate(request,id);
//
//        return ResponseEntity.ok(emailTemplateDto);
//
//    }
//
//    @DeleteMapping("/{id}")
//    public ResponseEntity<Void> deleteTemplate(@PathVariable Long id){
//        emailTemplateService.deleteTemplate(id);
//
//        return ResponseEntity.accepted().build();
//    }

}
