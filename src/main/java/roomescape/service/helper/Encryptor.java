package roomescape.service.helper;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import roomescape.domain.member.MemberPassword;
import roomescape.exception.NotFoundException;

@Component
public class Encryptor {

    private final String salt;

    public Encryptor(@Value("${security.salt}") String salt) {
        this.salt = salt;
    }

    public MemberPassword encryptPassword(String raw) {
        String rawAndSalt = raw + salt;
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            messageDigest.update(rawAndSalt.getBytes());
            return new MemberPassword(String.format("%064x", new BigInteger(1, messageDigest.digest())));
        } catch (NoSuchAlgorithmException e) {
            throw new NotFoundException("알고리즘을 찾을 수 없습니다.");
        }
    }
}
