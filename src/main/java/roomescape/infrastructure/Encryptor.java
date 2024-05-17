package roomescape.infrastructure;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import roomescape.service.exception.AlgorithmNotFoundException;

@Component
public class Encryptor {

    @Value("${security.password.salt}")
    private String salt;

    public String encryptPassword(final String raw) {
        final String rawAndSalt = raw + salt;
        try {
            final MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            messageDigest.update(rawAndSalt.getBytes());
            return String.format("%064x", new BigInteger(1, messageDigest.digest()));
        } catch (final NoSuchAlgorithmException e) {
            throw new AlgorithmNotFoundException("알고리즘을 찾을 수 없습니다.");
        }
    }
}
