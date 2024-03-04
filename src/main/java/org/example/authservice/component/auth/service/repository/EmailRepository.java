package org.example.authservice.component.auth.service.repository;

import org.example.authservice.component.auth.model.EmailEntity;
import org.springframework.data.repository.CrudRepository;

public interface EmailRepository extends CrudRepository<EmailEntity, Long> {

    boolean existsByEmail(String email);

    EmailEntity findByUserUsername(String username);
}
