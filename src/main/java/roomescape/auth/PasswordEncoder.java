package roomescape.auth;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import org.springframework.stereotype.Component;
import roomescape.exception.custom.status.ServerInternalException;

@Component
public class PasswordEncoder {

    private static final Base64.Encoder encoder = Base64.getEncoder();
    private static final Base64.Decoder decoder = Base64.getDecoder();

    private static final String PASSWORD_DELIMITER = ":";
    private static final String PASSWORD_FORMAT = "%s" + PASSWORD_DELIMITER + "%s";

    public String encode(final String password) {
        final byte[] passwordBytes = password.getBytes(StandardCharsets.UTF_8);
        final byte[] salt = generateSalt();
        return makeEncodedPassword(passwordBytes, salt);
    }

    public boolean matches(final String notEncodedPassword, final String password) {
        if (!password.contains(PASSWORD_DELIMITER)) {
            return false;
        }

        final byte[] salt = extractSalt(password);
        final String encodedPassword = makeEncodedPassword(notEncodedPassword.getBytes(StandardCharsets.UTF_8), salt);
        return encodedPassword.equals(password);
    }

    private String makeEncodedPassword(final byte[] passwordBytes, final byte[] salt) {
        final byte[] saltedPassword = sumSalt(passwordBytes, salt);
        final byte[] hashedBytes = getMessageDigest().digest(saltedPassword);
        return PASSWORD_FORMAT.formatted(
                encoder.encodeToString(hashedBytes),
                encoder.encodeToString(salt)
        );
    }

    private byte[] extractSalt(final String password) {
        return decoder.decode(password.split(PASSWORD_DELIMITER)[1]);
    }

    private MessageDigest getMessageDigest() {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (final Exception e) {
            throw new ServerInternalException();
        }
    }

    private byte[] generateSalt() {
        byte[] salt = new byte[16];
        new java.security.SecureRandom().nextBytes(salt);
        return salt;
    }

    private byte[] sumSalt(final byte[] passwordBytes, final byte[] salt) {
        final byte[] saltedPassword = new byte[passwordBytes.length + salt.length];
        System.arraycopy(passwordBytes, 0, saltedPassword, 0, passwordBytes.length);
        System.arraycopy(salt, 0, saltedPassword, passwordBytes.length, salt.length);
        return saltedPassword;
    }
}
