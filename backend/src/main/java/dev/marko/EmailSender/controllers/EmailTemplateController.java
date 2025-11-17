package dev.marko.EmailSender.controllers;

import dev.marko.EmailSender.controllers.base.BaseController;
import dev.marko.EmailSender.dtos.*;
import dev.marko.EmailSender.services.EmailTemplateService;
import dev.marko.EmailSender.services.base.BaseService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("templates")
public class EmailTemplateController extends BaseController<
        EmailTemplateDto,
        CreateTemplateRequest,
        UpdateTemplateRequest
        > {

    public EmailTemplateController(EmailTemplateService emailTemplateService) {
        super(emailTemplateService);
    }
}
