package org.example.authservice.component.auth.model;

import javax.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "ip_address_of_token_ref")
public class IpAddressEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ip_address_of_token_ip_seq")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "token_id")
    private TokenEntity token;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "is_confirmed")
    private Boolean isConfirmed;

}
