package io.notfound.counsel_back.user.repository;

import io.notfound.counsel_back.user.entity.User;
import io.notfound.counsel_back.user.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {
    Optional<UserProfile> findByUser(User user);
}
