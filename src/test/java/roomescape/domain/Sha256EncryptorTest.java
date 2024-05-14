package roomescape.domain;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class Sha256EncryptorTest {

    @Test
    @DisplayName("문자열 암호화 확인")
    void encrypt() {
        Sha256Encryptor sha256Encryptor = new Sha256Encryptor();
        String encrypted = sha256Encryptor.encrypt("qwer");
        Assertions.assertThat(encrypted)
                .isEqualTo("f6f2ea8f45d8a057c9566a33f99474da2e5c6a6604d736121650e2730c6fb0a3");
    }
}
