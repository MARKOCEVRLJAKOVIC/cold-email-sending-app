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

}
