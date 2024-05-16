package roomescape.domain.member;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Embeddable
public class Email {
    private static final Pattern ADDRESS_PATTERN = Pattern.compile("^\\w+@\\w+\\.\\w+$");
    private static final int ADDRESS_MAX_LENGTH = 50;

    @Column(name = "email", nullable = false)
    private String address;

    Email() {
    }

    public Email(String address) {
        validateLength(address);
        validatePattern(address);
        this.address = address;
    }

    private void validateLength(String address) {
        if (address != null && address.length() > ADDRESS_MAX_LENGTH) {
            throw new IllegalArgumentException("이메일은 50자 이하여야 합니다.");
        }
    }

    private void validatePattern(String address) {
        validateNonBlank(address);
        Matcher matcher = ADDRESS_PATTERN.matcher(address);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("이메일 형식이 올바르지 않습니다.");
        }
    }

    private void validateNonBlank(String address) {
        if (address == null || address.isBlank()) {
            throw new IllegalArgumentException("이메일은 필수 입력값 입니다.");
        }
    }

    public String getAddress() {
        return address;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Email other = (Email) o;
        return address.equals(other.address);
    }

    @Override
    public int hashCode() {
        return Objects.hash(address);
    }
}
