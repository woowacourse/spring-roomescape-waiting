package roomescape.member.domain;

import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import roomescape.global.exception.ViolationException;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Embeddable
@Access(AccessType.FIELD)
public class Email {
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[0-9a-zA-Z]([-_.]?[0-9a-zA-Z])*@[0-9a-zA-Z]([-_.]?[0-9a-zA-Z])*.[a-zA-Z]{2,3}$");

    @Column(nullable = false, unique = true, name = "email")
    private String value;

    protected Email() {
    }

    public Email(String value) {
        validateBlank(value);
        validatePattern(value);
        this.value = value;
    }

    private void validateBlank(String email) {
        if (email == null || email.isBlank()) {
            throw new ViolationException("이메일은 비어있을 수 없습니다.");
        }
        Matcher matcher = EMAIL_PATTERN.matcher(email);
        if (!matcher.matches()) {
            throw new ViolationException("이메일 형식에 맞지 않습니다.");
        }
    }

    private void validatePattern(String email) {
        Matcher matcher = EMAIL_PATTERN.matcher(email);
        if (!matcher.matches()) {
            throw new ViolationException("이메일 형식에 맞지 않습니다.");
        }
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Email that = (Email) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
