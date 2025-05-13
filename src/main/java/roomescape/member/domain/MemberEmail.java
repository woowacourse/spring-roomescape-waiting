package roomescape.member.domain;

import java.util.Objects;

public final class MemberEmail {

    private final String email;

    public MemberEmail(final String email) {
        validateEmail(email);
        this.email = email;
    }

    private void validateEmail(final String email) {
        final String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        if (email == null || email.isBlank() || !email.matches(emailRegex)) {
            throw new IllegalArgumentException("이메일 형식으로 입력해야 합니다.");
        }
    }

    public String getEmail() {
        return email;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final MemberEmail email1 = (MemberEmail) o;
        return Objects.equals(email, email1.email);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(email);
    }
}
