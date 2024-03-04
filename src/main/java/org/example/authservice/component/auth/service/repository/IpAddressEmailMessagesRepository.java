package org.example.authservice.component.auth.service.repository;

import org.example.authservice.component.auth.model.IpAddressEmailMessagesEntity;
import org.springframework.data.repository.CrudRepository;

public interface IpAddressEmailMessagesRepository extends CrudRepository<IpAddressEmailMessagesEntity, Long> {

    public IpAddressEmailMessagesEntity getByIpAddress(String ipAddress);
}
