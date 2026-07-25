package com.holdencase.portfolio.controller;

import com.holdencase.portfolio.dto.ContactForm;
import com.holdencase.portfolio.service.ContactService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ContactController {

    private final ContactService contactService;

    public ContactController(ContactService contactService) {
        this.contactService = contactService;
    }

    @GetMapping("/contact")
    public String showForm(Model model) {
        if (!model.containsAttribute("contactForm")) {
            model.addAttribute("contactForm", new ContactForm());
        }
        return "contact";
    }

    @PostMapping("/contact")
    public String submit(@Valid @ModelAttribute("contactForm") ContactForm form,
                          BindingResult result,
                          HttpServletRequest request,
                          RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "contact";
        }

        String clientIp = request.getRemoteAddr();
        ContactService.Outcome outcome = contactService.submit(form, clientIp);

        switch (outcome) {
            case SAVED, SPAM_REJECTED -> redirectAttributes.addFlashAttribute("success", true);
            case RATE_LIMITED -> redirectAttributes.addFlashAttribute("rateLimited", true);
        }

        return "redirect:/contact";
    }
}
