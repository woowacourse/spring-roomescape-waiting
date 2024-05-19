package roomescape.domain;

import jakarta.persistence.Embeddable;

import java.util.Objects;
import java.util.regex.Pattern;

@Embeddable
public class Email {

    private static final Pattern EMAIL_REGEX = Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");

    private String email;

    protected Email() {
    }

    public Email(String email) {
        validateEmail(email);
        this.email = email;
    }

    private void validateEmail(String email) {
        if (email == null || !EMAIL_REGEX.matcher(email).matches()) {
            throw new IllegalArgumentException("잘못된 이메일 형식입니다.");
        }
    }

    public String getEmail() {
        return email;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        Email email1 = (Email) object;
        return Objects.equals(email, email1.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(email);
    }
}
