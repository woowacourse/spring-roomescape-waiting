package roomescape.member.domain;

import static roomescape.common.auth.exception.AuthExceptionInformation.INTERNAL_SERVER_CRYPTO_ERROR;
import static roomescape.member.exception.MemberExceptionInformation.PASSWORD_NOT_MATCH;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import roomescape.common.auth.exception.AuthException;
import roomescape.member.exception.MemberException;

@Embeddable
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor
public class Password {

    @Column(name = "password")
    private String value;

    public static Password from(String password) {
        return new Password(encrypt(password));
    }

    public static Password load(String encryptedPassword) {
        return new Password(encryptedPassword);
    }

    private static String encrypt(String password) {
        try {
            final String algorithm = "SHA-256";
            MessageDigest md = MessageDigest.getInstance(algorithm);
            byte[] bytes = password.getBytes();
            byte[] digest = md.digest(bytes);
            return convertHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new AuthException(INTERNAL_SERVER_CRYPTO_ERROR);
        }
    }

    public static String convertHex(byte[] rawHmac) {
        StringBuilder sb = new StringBuilder();
        for (byte byteData : rawHmac) {
            sb.append(String.format("%02x", byteData));
        }
        return sb.toString();
    }

    public void validateMatches(String password) {
        if (isNotMatches(password)) {
            throw new MemberException(PASSWORD_NOT_MATCH);
        }
    }

    private boolean isNotMatches(String password) {
        return !encrypt(password).equals(this.value);
    }
}
