
package com.example.carteira.repository;

import com.example.carteira.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean  existsByDocumento(String documento);
    boolean  existsByEmail(String email);

    Optional<User> findByEmail(String email);
    Optional<User> findByDocumento(String documento);
}
