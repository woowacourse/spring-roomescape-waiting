package roomescape.member.domain;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import roomescape.global.exception.InvalidArgumentException;

@Embeddable
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Password {

    private static final int PASSWORD_MAX_LENGTH = 25;
    private String password;

    public static Password encrypt(String rawPassword, PasswordEncryptor passwordEncoder) {
        validateRawPassword(rawPassword);
        return new Password(passwordEncoder.encrypt(rawPassword));
    }

    private static void validateRawPassword(String rawPassword) {
        if (rawPassword == null) {
            throw new InvalidArgumentException("비밀번호는 null일 수 없습니다.");
        }

        if (rawPassword.length() > PASSWORD_MAX_LENGTH || rawPassword.isBlank()) {
            throw new InvalidArgumentException("비밀번호는 공백이거나 25자 이상일 수 없습니다.");
        }
    }
}
