package com.holdencase.portfolio.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ContactForm {

    @NotBlank(message = "Please enter your name.")
    @Size(max = 100)
    private String name;

    @NotBlank(message = "Please enter your email address.")
    @Email(message = "Please enter a valid email address.")
    @Size(max = 150)
    private String email;

    @NotBlank(message = "Please enter a subject.")
    @Size(max = 150)
    private String subject;

    @NotBlank(message = "Please enter a message.")
    @Size(max = 2000, message = "Message must be under 2000 characters.")
    private String message;

    /**
     * Honeypot: left empty by real users, hidden off-screen via CSS.
     * Any non-blank value here marks the submission as spam.
     */
    private String website;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }
}
