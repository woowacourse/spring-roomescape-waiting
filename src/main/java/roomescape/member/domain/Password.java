package roomescape.member.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import roomescape.member.exception.PasswordRequiredException;

@Embeddable
@NoArgsConstructor
@Getter
@EqualsAndHashCode
public class Password {
    @Column(name = "password", nullable = false)
    private String value;

    public Password(String rawValue, PasswordEncoder encoder) {
        validatePassword(rawValue);
        this.value = encoder.encode(rawValue);
    }

    private void validatePassword(String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            throw new PasswordRequiredException();
        }
    }
}
