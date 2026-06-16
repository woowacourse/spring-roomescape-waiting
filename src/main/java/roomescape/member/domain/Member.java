package roomescape.member.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor
@Entity(name = "member")
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    @Embedded
    private Password password;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role;

    public static Member register(String name, String password) {
        return new Member(null, name, Password.from(password), Role.MEMBER);
    }

    public static Member load(Long id, String name, String encryptedPassword, Role role) {
        return new Member(id, name, Password.load(encryptedPassword), role);
    }

    public void matchPassword(String inputPassword) {
        password.validateMatches(inputPassword);
    }

}
