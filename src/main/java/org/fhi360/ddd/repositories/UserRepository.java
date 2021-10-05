package org.fhi360.ddd.repositories;

import org.fhi360.ddd.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsernameAndPasswordIgnoreCase(String username,String password);


}
