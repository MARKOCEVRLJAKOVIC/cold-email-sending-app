package dev.marko.EmailSender.controllers;

import dev.marko.EmailSender.controllers.base.BaseController;
import dev.marko.EmailSender.dtos.*;
import dev.marko.EmailSender.services.EmailTemplateService;
import org.springframework.web.bind.annotation.*;

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
