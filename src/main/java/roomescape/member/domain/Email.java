package roomescape.member.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.util.Objects;
import java.util.regex.Pattern;
import roomescape.exception.BadRequestException;
import roomescape.exception.ErrorType;

@Embeddable
public class Email {

    private static final String EMAIL_REGEX = "^.*@.*\\..*$";

    private static final Pattern pattern = Pattern.compile(EMAIL_REGEX);

    @Column(name = "email", nullable = false)
    private String address;

    public Email(String address) {
        validate(address);
        this.address = address;
    }

    public Email() {
    }

    public void validate(String email) {
        if (email == null || !pattern.matcher(email).matches()) {
            throw new BadRequestException(ErrorType.EMAIL_FORMAT_ERROR);
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
        Email email1 = (Email) o;
        return Objects.equals(getAddress(), email1.getAddress());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getAddress());
    }

    @Override
    public String toString() {
        return "Email{" +
                "email='" + address + '\'' +
                '}';
    }
}

