package roomescape.domain;

import org.springframework.stereotype.Component;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

@Component
public class SHA256PasswordEncoder implements PasswordEncoder {
    @Override
    public Password encode(final String rawPassword, final String salt) {
        return new Password(hashPassword(rawPassword, salt), salt);
    }

    @Override
    public Password encode(final String rawPassword) {
        String salt = generateSalt();
        return encode(rawPassword, salt);
    }

    private String generateSalt() {
        byte[] salt = new byte[16];
        try {
            SecureRandom sr = SecureRandom.getInstanceStrong();
            sr.nextBytes(salt);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        return Base64.getEncoder().encodeToString(salt);
    }

    private String hashPassword(String password, String salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(Base64.getDecoder().decode(salt));
            byte[] hashedPassword = md.digest(password.getBytes());
            return Base64.getEncoder().encodeToString(hashedPassword);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
