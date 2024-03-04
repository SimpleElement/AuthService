package org.example.authservice.component.auth.model;

import javax.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "ip_address_email_messages_ref")
public class IpAddressEmailMessagesEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ip_address_email_messages_id_seq")
    private Long id;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "count_write_message")
    private Integer countWriteMessage;

    @Column(name = "first_write_message")
    private LocalDateTime firstWriteMessage;

}
