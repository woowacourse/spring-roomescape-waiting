package roomescape.global;

import java.util.Base64;
import org.springframework.stereotype.Component;

@Component
public class PasswordEncoder {

    public String encode(final String rawPassword) {
        return Base64.getEncoder().encodeToString(rawPassword.getBytes());
    }

    public boolean matches(final String rawPassword, final String encodedPassword) {
        return encode(rawPassword).equals(encodedPassword);
    }
}
