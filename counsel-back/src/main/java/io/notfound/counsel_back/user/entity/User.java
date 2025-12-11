package io.notfound.counsel_back.user.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.Collections;

@Entity // JPA 엔티티로 지정
@Table(name = "users") // 테이블 이름 지정 (user는 예약어일 수 있어 users로 사용)
@Getter // Lombok: 모든 필드의 Getter 메서드 자동 생성
@Builder // Lombok: 빌더 패턴을 사용하여 객체 생성 가능
@NoArgsConstructor // Lombok: 기본 생성자 자동 생성
@AllArgsConstructor // Lombok: 모든 필드를 포함하는 생성자 자동 생성
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING) // Enum 타입을 DB에 문자열로 저장
    private UserRole role;

    // Spring Security의 GrantedAuthority를 반환하는 헬퍼 메서드
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + this.role.name()));
    }
}