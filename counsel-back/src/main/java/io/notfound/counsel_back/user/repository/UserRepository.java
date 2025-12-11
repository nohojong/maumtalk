package io.notfound.counsel_back.user.repository;

import io.notfound.counsel_back.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository // Spring Bean으로 등록
public interface UserRepository extends JpaRepository<User, Long> {

    // Spring Data JPA가 자동으로 쿼리를 생성해주는 메서드
    Optional<User> findByEmail(String email);
}