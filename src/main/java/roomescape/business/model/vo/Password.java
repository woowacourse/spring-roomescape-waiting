package roomescape.business.model.vo;

import jakarta.persistence.Column;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

public record Password(
        @Column(name = "password")
        String value
) {
    private static final PasswordEncoder encoder = new BCryptPasswordEncoder();

    public static Password encode(final String rawValue) {
        String encodedPassword = encoder.encode(rawValue);
        return new Password(encodedPassword);
    }

    public static Password plain(final String encodedValue) {
        return new Password(encodedValue);
    }

    public boolean matches(final String rawValue) {
        return encoder.matches(rawValue, value);
    }
}
