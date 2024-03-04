package org.example.authservice.component.auth.service.repository;

import org.example.authservice.component.auth.model.UserEntity;
import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends CrudRepository<UserEntity, Long> {

    boolean existsByUsername(String username);

    UserEntity findByUsername(String username);

    UserEntity findByUsernameAndPassword(String username, String password);
}
