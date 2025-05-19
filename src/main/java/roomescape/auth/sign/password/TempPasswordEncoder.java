package roomescape.auth.sign.password;

import org.springframework.stereotype.Component;

@Component
public class TempPasswordEncoder implements PasswordEncoder {

    public static final String secretKey = "(단방향) 해시 처리 완료";

    @Override
    public Password execute(final String rawPassword) {
        return Password.fromEncoded(rawPassword + secretKey);
    }
}
