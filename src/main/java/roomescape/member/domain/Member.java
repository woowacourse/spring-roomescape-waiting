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
import roomescape.global.exception.InvalidArgumentException;

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
    @Embedded
    private Email email;
    @Embedded
    private Password password;

    private Member(Long id, String name, Role role, Email email, Password password) {
        this.id = id;
        this.name = name;
        this.role = role;
        this.email = email;
        this.password = password;
        validate();
    }

    public void validate() {
        if (name == null || role == null || password == null) {
            throw new InvalidArgumentException("Member의 필드는 null 일 수 없습니다.");
        }
    }

    public static Member signUpUser(String name, Email email, Password password) {
        return new Member(null, name, Role.USER, email, password);
    }

    public static Member signUpAdmin(String name, Email email, Password password) {
        return new Member(null, name, Role.ADMIN, email, password);
    }

    public String getEmail() {
        return email.email();
    }
}
