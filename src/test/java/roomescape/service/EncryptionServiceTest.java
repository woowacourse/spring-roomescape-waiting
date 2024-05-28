package roomescape.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.IntegrationTestSupport;

import static org.assertj.core.api.Assertions.assertThat;

class EncryptionServiceTest extends IntegrationTestSupport {

    @Autowired
    EncryptionService encryptionService;

    @ParameterizedTest
    @ValueSource(strings = {"qwer1234", "486", "password"})
    @DisplayName("비밀번호를 암호화한다.")
    void encryption(final String rawPassword) {
        final String encryptPassword = encryptionService.encryptPassword(rawPassword);

        assertThat(encryptPassword).hasSize(64);
    }
}
