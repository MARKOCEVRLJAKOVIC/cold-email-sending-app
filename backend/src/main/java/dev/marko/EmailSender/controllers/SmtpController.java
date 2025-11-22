package dev.marko.EmailSender.controllers;

import dev.marko.EmailSender.controllers.base.BaseController;
import dev.marko.EmailSender.dtos.RegisterEmailRequest;
import dev.marko.EmailSender.dtos.SmtpDto;
import dev.marko.EmailSender.dtos.UpdateSmtpRequest;
import dev.marko.EmailSender.services.SmtpService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/smtp")
public class SmtpController extends BaseController<
        SmtpDto,
        RegisterEmailRequest,
        UpdateSmtpRequest
        > {

    public SmtpController(
            SmtpService service
    ) {
        super(service);
    }
}
