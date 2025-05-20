package roomescape.member.infrastructure.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.springframework.stereotype.Component;
import roomescape.member.domain.PasswordEncryptor;

@Component
public class Sha256PasswordEncryptor implements PasswordEncryptor {

    @Override
    public String encrypt(final String rawPassword) {
        try {
            final MessageDigest digest = MessageDigest.getInstance("SHA-256");
            final byte[] hash = digest.digest(rawPassword.getBytes(StandardCharsets.UTF_8));
            final StringBuilder hexString = new StringBuilder();
            for (final byte b : hash) {
                final String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (final NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 암호화 실패", e);
        }
    }
}
