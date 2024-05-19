package roomescape.domain;

import static roomescape.exception.ExceptionType.EMPTY_NAME;
import static roomescape.exception.ExceptionType.INVALID_EMAIL_FORMAT;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import roomescape.exception.RoomescapeException;

class MemberTest {
    private final Sha256Encryptor encryptor = new Sha256Encryptor();

    @Test
    @DisplayName("회원 생성 시 이름이 비어있는 경우 예외 발생하는지 확인")
    void createWithEmptyName() {
        String password = "1234";
        String encrypted = encryptor.encrypt(password);
        Assertions.assertThatThrownBy(() -> new Member("", "example@example.com", encrypted))
                .isInstanceOf(RoomescapeException.class)
                .hasMessage(EMPTY_NAME.getMessage());
    }

    @Test
    @DisplayName("회원 생성 시 이름이 없는 경우 예외 발생하는지 확인")
    void createWithNullName() {
        String password = "1234";
        String encrypted = encryptor.encrypt(password);
        Assertions.assertThatThrownBy(() -> new Member(null, "example@example.com", encrypted))
                .isInstanceOf(RoomescapeException.class)
                .hasMessage(EMPTY_NAME.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"email.com", "test#test.com", "test@test", ""})
    @DisplayName("회원 생성 시 이메일이 잘못된 형식인 경우 예외 발생하는지 확인")
    void createWithInvalidEmail(String email) {
        String password = "1234";
        String encrypted = encryptor.encrypt(password);
        Assertions.assertThatThrownBy(() -> new Member("name", email, encrypted))
                .isInstanceOf(RoomescapeException.class)
                .hasMessage(INVALID_EMAIL_FORMAT.getMessage());
    }

    @Test
    @DisplayName("회원 생성 시 이메일이 없는 경우 예외 발생하는지 확인")
    void createWithNullEmail() {
        String password = "1234";
        String encrypted = encryptor.encrypt(password);
        Assertions.assertThatThrownBy(() -> new Member("name", null, encrypted))
                .isInstanceOf(RoomescapeException.class)
                .hasMessage(INVALID_EMAIL_FORMAT.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"123456", "f6f2ea8f45d8a057c9566a33f99474da2e5c6a6604d736121650e2730c6fb0a"})
    @DisplayName("회원 생성 시 비밀번호가 제대로 암호화되어있지 않은 경우 예외 발생하는지 확인")
    void createWithRawPassword(String password) {
        Assertions.assertThatThrownBy(() -> new Member("name", "email@email.com", password))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("암호화된 비밀번호로 생성하세요!");
    }

    @Test
    @DisplayName("회원 생성 시 비밀번호가 없는 경우 예외 발생하는지 확인")
    void createWithNullPassword() {
        Assertions.assertThatThrownBy(() -> new Member("name", "email@email.com", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("암호화된 비밀번호로 생성하세요!");
    }
}
