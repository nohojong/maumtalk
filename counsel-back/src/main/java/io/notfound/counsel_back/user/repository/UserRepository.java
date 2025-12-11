package io.notfound.counsel_back.user.repository;

import io.notfound.counsel_back.user.entity.ProviderType;
import io.notfound.counsel_back.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByProviderId(String providerId);

    Optional<User> findByEmailAndProvider(String email, ProviderType provider);

    boolean existsByEmail(String email);

    boolean existsByProviderId(String providerId);

    boolean existsByEmailAndProvider(String email, ProviderType provider);
}