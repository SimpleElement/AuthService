package org.example.authservice.component.auth.service.repository;

import org.example.authservice.component.auth.model.ConfirmIpAddressEntity;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.CrudRepository;

import javax.transaction.Transactional;

public interface ConfirmIpAddressRepository extends CrudRepository<ConfirmIpAddressEntity, Long> {

    @Modifying
    @Transactional
    void removeAllByIpAddressTokenToken(String token);

    ConfirmIpAddressEntity findByIpAddressTokenUserUsernameAndIpAddressIpAddress(String username, String ipAddress);
}
