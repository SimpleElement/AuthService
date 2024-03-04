package org.example.authservice.component.auth.model;

import javax.persistence.*;

import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "token_of_user_ref")
public class TokenEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "token_of_user_id_seq")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @Column(name = "token")
    private String token;

    @Column(name = "user_agent")
    private String userAgent;

    @Column(name = "last_active")
    private LocalDateTime lastActive;

}
