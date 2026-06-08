package roomescape.common.security;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import java.util.regex.Pattern;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import org.springframework.stereotype.Component;

@Component
public class Pbkdf2PasswordEncoder implements PasswordEncoder {

    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final String DELIMITER = "$";

    private static final int SALT_LENGTH = 16;
    private static final int ITERATIONS = 100_000;
    private static final int KEY_LENGTH = 256;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    @Override
    public String encode(String rawPassword) {
        if (rawPassword == null || rawPassword.isBlank()) {
            throw new IllegalStateException("암호화할 비밀번호는 공백일 수 없습니다.");
        }
        byte[] salt = generateSalt();
        byte[] hash = hash(rawPassword, salt, ITERATIONS, KEY_LENGTH);

        return String.join(
                DELIMITER,
                ALGORITHM,
                String.valueOf(ITERATIONS),
                encodeBase64(salt),
                encodeBase64(hash));
    }

    @Override
    public boolean matches(String rawPassword, String encodedPassword) {
        String[] parts = encodedPassword.split(Pattern.quote(DELIMITER));

        if (parts.length != 4) {
            return false;
        }

        String algorithm = parts[0];
        int iterations = Integer.parseInt(parts[1]);
        byte[] salt = decodeBase64(parts[2]);
        byte[] savedHash = decodeBase64(parts[3]);

        if (!ALGORITHM.equals(algorithm)) {
            return false;
        }

        byte[] rawHash = hash(rawPassword, salt, iterations, savedHash.length * 8);

        return MessageDigest.isEqual(savedHash, rawHash);
    }

    private byte[] generateSalt() {
        byte[] salt = new byte[SALT_LENGTH];
        SECURE_RANDOM.nextBytes(salt);
        return salt;
    }

    private byte[] hash(String rawPassword, byte[] salt, int iterations, int keyLength) {
        try {
            PBEKeySpec spec = new PBEKeySpec(
                    rawPassword.toCharArray(),
                    salt,
                    iterations,
                    keyLength);

            SecretKeyFactory factory = SecretKeyFactory.getInstance(ALGORITHM);
            return factory.generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException("비밀번호 해싱에 실패했습니다.", e);
        }
    }

    private String encodeBase64(byte[] bytes) {
        return Base64.getEncoder().encodeToString(bytes);
    }

    private byte[] decodeBase64(String value) {
        return Base64.getDecoder().decode(value);
    }
}
