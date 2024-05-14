package roomescape.domain;

import io.micrometer.common.util.StringUtils;
import java.util.Objects;
import java.util.regex.Pattern;

public class MemberEmail {

    private static final Pattern VALID_EMAIL_ADDRESS_REGEX =
        Pattern.compile("^[A-z0-9]+@[A-z0-9.-]+\\.[A-z]{2,6}$");

    private final String email;

    public MemberEmail(String email) {
        validate(email);
        this.email = email;
    }

    private void validate(String value) {
        if (StringUtils.isBlank(value) || !VALID_EMAIL_ADDRESS_REGEX.matcher(value).matches()) {
            throw new IllegalArgumentException("이메일 형식이 아닙니다.");
        }
    }

    public String getEmail() {
        return email;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MemberEmail memberEmail = (MemberEmail) o;
        return Objects.equals(email, memberEmail.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(email);
    }
}
