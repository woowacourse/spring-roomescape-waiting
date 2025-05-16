package roomescape.member.domain;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import roomescape.global.exception.InvalidArgumentException;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Member {

    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    @Enumerated(value = EnumType.STRING)
    private Role role;
    private String email;
    @Embedded
    private Password password;

    @Builder
    private Member(Long id, String name, Role role, String email, Password password) {
        this.id = id;
        this.name = name;
        this.role = role;
        this.email = email;
        this.password = password;
        validate();
    }

    public static Member signUpUser(String name, String email, Password password) {
        return Member.builder()
                .name(name)
                .role(Role.USER)
                .email(email)
                .password(password)
                .build();
    }

    public static Member signUpAdmin(String name, String email, Password password) {
        return Member.builder()
                .name(name)
                .role(Role.ADMIN)
                .email(email)
                .password(password)
                .build();
    }

    public void validate() {
        if (name == null || role == null || password == null) {
            throw new InvalidArgumentException("Member의 필드는 null 일 수 없습니다.");
        }

        validateEmail();
    }

    public void validateEmail() {
        if (email == null) {
            throw new InvalidArgumentException("이메일은 null 일 수 없습니다.");
        }

        if (!email.matches(EMAIL_REGEX)) {
            throw new InvalidArgumentException("이메일 형식이 아닙니다.");
        }
    }
}
