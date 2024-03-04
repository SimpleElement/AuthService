package org.example.authservice.component.auth.service.repository;

import org.example.authservice.component.auth.model.TokenEntity;
import org.springframework.data.repository.CrudRepository;

public interface TokenRepository extends CrudRepository<TokenEntity, Long> {
    TokenEntity getByToken(String token);
    TokenEntity getByUserUsername(String username);
}
