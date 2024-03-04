package org.example.authservice.component.auth.model;

import javax.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "email_of_user_ref")
public class EmailEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "email_of_user_id_seq")
    private Long id;

    @OneToOne(optional = false)
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @Column(name = "email")
    private String email;

    @Column(name = "is_confirmed")
    private Boolean isConfirmed;
}
