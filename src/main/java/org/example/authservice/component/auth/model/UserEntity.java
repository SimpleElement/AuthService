package org.example.authservice.component.auth.model;

import javax.persistence.*;
import lombok.Data;
@Data
@Entity
@Table(name = "user_ref")
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_seq")
    private Long id;

    @Column(name = "username")
    private String username;

    @Column(name = "password")
    private String password;

    @OneToOne(mappedBy = "user", fetch=FetchType.LAZY)
    private EmailEntity email;
}
