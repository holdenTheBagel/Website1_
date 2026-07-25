package com.holdencase.portfolio.controller;

import com.holdencase.portfolio.service.ContactService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(ContactController.class)
class ContactControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ContactService contactService;

    @Test
    void showFormRendersContactPage() throws Exception {
        mockMvc.perform(get("/contact"))
                .andExpect(status().isOk())
                .andExpect(view().name("contact"));
    }

    @Test
    void invalidSubmissionReRendersFormWithErrorsAndDoesNotCallService() throws Exception {
        mockMvc.perform(post("/contact")
                        .param("name", "")
                        .param("email", "not-an-email")
                        .param("subject", "")
                        .param("message", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("contact"))
                .andExpect(model().attributeHasFieldErrors("contactForm", "name", "email", "subject", "message"));

        verify(contactService, never()).submit(any(), anyString());
    }

    @Test
    void validSubmissionSavesAndRedirectsWithSuccessFlash() throws Exception {
        when(contactService.submit(any(), anyString())).thenReturn(ContactService.Outcome.SAVED);

        mockMvc.perform(post("/contact")
                        .param("name", "Jane Doe")
                        .param("email", "jane@example.com")
                        .param("subject", "Project inquiry")
                        .param("message", "I'd like to discuss a project."))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/contact"))
                .andExpect(flash().attribute("success", true));

        verify(contactService).submit(any(), anyString());
    }
}
