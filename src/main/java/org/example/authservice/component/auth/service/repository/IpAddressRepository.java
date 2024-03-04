package org.example.authservice.component.auth.service.repository;

import org.example.authservice.component.auth.model.IpAddressEntity;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.CrudRepository;

import javax.transaction.Transactional;

public interface IpAddressRepository extends CrudRepository<IpAddressEntity, Long> {
    IpAddressEntity findByIpAddressAndTokenToken(String ip, String token);

    IpAddressEntity findByIpAddressAndTokenUserUsername(String ip, String username);

    @Modifying
    @Transactional
    void removeAllByTokenToken(String token);

    @Modifying
    @Transactional
    void removeAllByTokenUserUsername(String username);
}
