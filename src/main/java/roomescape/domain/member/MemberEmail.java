package roomescape.domain.member;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.util.Objects;
import java.util.regex.Pattern;
import roomescape.exception.InvalidRequestException;

@Embeddable
public class MemberEmail {

    private static final String EMAIL_REGEX =
            "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,}$";
    private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);

    @Column(name = "email", nullable = false)
    private String value;

    public MemberEmail() {
    }

    public MemberEmail(String value) {
        validateNullOrBlank(value);
        validateEmailPattern(value);
        this.value = value;
    }

    private void validateNullOrBlank(String value) {
        if (value == null || value.isBlank()) {
            throw new InvalidRequestException("이메일을 입력해주세요.");
        }
    }

    private void validateEmailPattern(String value) {
        if (!EMAIL_PATTERN.matcher(value).matches()) {
            throw new InvalidRequestException("올바른 이메일 형식이 아닙니다.");
        }
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MemberEmail that = (MemberEmail) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }
}
