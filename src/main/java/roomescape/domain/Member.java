package roomescape.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import roomescape.exception.UnableCreateMemberException;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member {

    private static final String EMAIL_RULE_REGEX = "^[\\w._%+-]+@[\\w.-]+\\.[a-zA-Z]{2,}$";
    private static final String PASSWORD_RULE_REGEX = "^(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]).+$";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Enumerated(EnumType.STRING)
    private MemberRole role;

    private String email;

    private String password;

    public Member(final Long id, final String name, final MemberRole role, final String email, final String password) {
        this.id = id;
        this.name = Objects.requireNonNull(name, "회원 이름은 null일 수 없습니다.");
        this.role = Objects.requireNonNull(role, "회원 권한은 null일 수 없습니다.");
        this.email = Objects.requireNonNull(email, "이메일은 null일 수 없습니다.");
        this.password = Objects.requireNonNull(password, "비밀번호는 null일 수 없습니다.");

        validateName(name);
        validateEmail(email);
        validatePassword(password);
    }

    public Member(final String name, final MemberRole role, final String email, final String password) {
        this(null, name, role, email, password);
    }

    public boolean isPasswordMatched(final String password) {
        return this.password.equals(password);
    }

    public boolean isPasswordNotMatched(final String password) {
        return !isPasswordMatched(password);
    }

    private void validateName(final String name) {
        if (name.length() < 2 || name.length() > 10) {
            throw new UnableCreateMemberException("회원 이름은 2글자에서 10글자까지만 가능합니다.");
        }
    }

    private void validateEmail(final String email) {
        if (!email.matches(EMAIL_RULE_REGEX)) {
            throw new UnableCreateMemberException("유효한 이메일 주소를 입력해주세요.");
        }
    }

    private void validatePassword(final String password) {
        if (password.length() < 8 || !password.matches(PASSWORD_RULE_REGEX)) {
            throw new UnableCreateMemberException("비밀번호는 최소 8글자 이상, 하나 이상의 대문자와 숫자, 특수문자를 포함해야 합니다.");
        }
    }

    @Override
    public boolean equals(final Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Member member = (Member) o;
        return id != null && Objects.equals(id, member.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
