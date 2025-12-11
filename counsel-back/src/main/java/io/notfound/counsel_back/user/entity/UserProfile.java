package io.notfound.counsel_back.user.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_profile")
@Getter
@Setter
@NoArgsConstructor // public 생성자로 변경
@AllArgsConstructor
@Builder
public class UserProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String gender;
    private Integer age;
    private String interests;
    private String concern;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    public void updateProfile(String gender, Integer age, String interests, String concern) {
        if (gender != null) this.gender = gender;
        if (age != null) this.age = age;
        if (interests != null) this.interests = interests;
        if (concern != null) this.concern = concern;
    }
}
