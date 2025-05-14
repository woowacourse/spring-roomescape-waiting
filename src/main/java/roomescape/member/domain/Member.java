package roomescape.member.domain;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    @Enumerated(value = EnumType.STRING)
    private Role role;
    private String email;
    @Embedded
    private Password password;

    private Member(Long id, String name, Role role, String email, Password password) {
        this.id = id;
        this.name = name;
        this.role = role;
        this.email = email;
        this.password = password;
    }

    public static Member signUpUser(String name, String email, Password password) {
        return new Member(null, name, Role.USER, email, password);
    }

    public static Member signUpAdmin(String name, String email, Password password) {
        return new Member(null, name, Role.ADMIN, email, password);
    }
}
