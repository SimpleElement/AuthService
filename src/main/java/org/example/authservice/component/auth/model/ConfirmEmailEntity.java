package org.example.authservice.component.auth.model;

import javax.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "confirm_email_ref")
public class ConfirmEmailEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "confirm_email_ref_id__seq")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "email_id")
    private EmailEntity email;

    @Column(name = "confirm_code")
    private String confirmCode;

    @Column(name = "number_of_confirmation_attempts")
    private Integer numberOfConfirmationAttempts;

    @Column(name = "last_active")
    private LocalDateTime registrationTime;
}
