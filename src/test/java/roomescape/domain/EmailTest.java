package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThatCode;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import roomescape.exception.RoomescapeErrorCode;
import roomescape.exception.RoomescapeException;

public class EmailTest {

    @DisplayName("이메일의 길이와 형식이 올바르면 예외가 발생하지 않는다.")
    @Test
    void createEmail() {
        assertThatCode(() -> new Email("her0807zzang@wooteco.com"))
                .doesNotThrowAnyException();
    }

    @DisplayName("이메일이 null이거나 공백이면 예외가 발생한다.")
    @NullAndEmptySource
    @ParameterizedTest
    void createNullOrEmptyException(String email) {
        assertThatCode(() -> new Email(email))
                .isInstanceOf(RoomescapeException.class)
                .extracting("errorCode")
                .isEqualTo(RoomescapeErrorCode.BAD_REQUEST);
    }

    @DisplayName("이메일 형식이 올바르지 않은 경우 예외가 발생한다.")
    @Test
    void createInvalidEmailPattern() {
        String email = "google.com";
        assertThatCode(() -> new Email(email))
                .isInstanceOf(RoomescapeException.class)
                .extracting("errorCode")
                .isEqualTo(RoomescapeErrorCode.BAD_REQUEST);
    }

    @DisplayName("이메일의 길이가 최소 11자 미만이거나 40자를 초과하면 예외가 발생한다.")
    @ParameterizedTest
    @ValueSource(strings = {"a@nate.com", "abcdefghijklmnopqrstuvwxyzzzzz@google.com"})
    void createInvalidEmailLength(String email) {
        assertThatCode(() -> new Email(email))
                .isInstanceOf(RoomescapeException.class)
                .extracting("errorCode")
                .isEqualTo(RoomescapeErrorCode.BAD_REQUEST);
    }
}
