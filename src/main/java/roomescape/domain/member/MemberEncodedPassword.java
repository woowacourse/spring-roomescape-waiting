package roomescape.domain.member;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.util.Objects;
import org.springframework.security.crypto.password.PasswordEncoder;

@Embeddable
public record MemberEncodedPassword(
        @Column(nullable = false)
        String password
) {

    public MemberEncodedPassword(final String password) {
        this.password = Objects.requireNonNull(password, "password은 null일 수 없습니다.");
    }

    public boolean isMatched(final MemberPassword rawPassword, final PasswordEncoder encoder) {
        return encoder.matches(rawPassword.password(), password);
    }
}
