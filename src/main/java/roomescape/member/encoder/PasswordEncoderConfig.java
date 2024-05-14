package roomescape.member.encoder;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PasswordEncoderConfig {

    @Value("${custom.security.password-encode.hash-salt}")
    private byte[] salt;
    @Value("${custom.security.password-encode.iteration-count}")
    private int iterationCount;
    @Value("${custom.security.password-encode.key-length}")
    private int keyLength;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new Pbkdf2PasswordEncoder(salt, iterationCount, keyLength);
    }
}
