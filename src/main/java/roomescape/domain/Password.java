package roomescape.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import roomescape.exception.NonEncryptedPassword;

@Embeddable
public class Password {
    private static final Pattern SHA_256_PATTERN = Pattern.compile("[a-fA-F0-9]{64}");

    @Column(name = "password")
    private String encryptedPassword;

    protected Password() {
    }

    public Password(String encryptedPassword) {
        validate(encryptedPassword);
        this.encryptedPassword = encryptedPassword;
    }

    private void validate(String encryptedPassword) {
        String errorMessage = "암호화된 비밀번호로 생성하세요!";
        if (encryptedPassword == null) {
            throw new NonEncryptedPassword(errorMessage);
        }
        Matcher matcher = SHA_256_PATTERN.matcher(encryptedPassword);
        if (!matcher.matches()) {
            throw new NonEncryptedPassword(errorMessage);
        }
    }

    public String getEncryptedPassword() {
        return encryptedPassword;
    }
}
