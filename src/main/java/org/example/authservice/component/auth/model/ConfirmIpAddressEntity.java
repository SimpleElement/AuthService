package org.example.authservice.component.auth.model;

import javax.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "confirm_ip_address_ref")
public class ConfirmIpAddressEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "confirm_ip_addresses_id_seq")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "ip_address_id")
    private IpAddressEntity ipAddress;

    @Column(name = "confirm_code")
    private String confirmCode;

    @Column(name = "number_of_confirmation_attempts")
    private Integer numberOfConfirmationAttempts;

}
