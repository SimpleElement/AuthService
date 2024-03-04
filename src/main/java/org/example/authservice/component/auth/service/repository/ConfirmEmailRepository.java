package org.example.authservice.component.auth.service.repository;

import org.example.authservice.component.auth.model.ConfirmEmailEntity;
import org.springframework.data.repository.CrudRepository;

public interface ConfirmEmailRepository extends CrudRepository<ConfirmEmailEntity, Long> {

    ConfirmEmailEntity findByEmailUserUsername(String username);
}
