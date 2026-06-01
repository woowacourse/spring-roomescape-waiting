package roomescape.reservation.domain;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import roomescape.global.exception.RoomEscapeException;

class UserTest {

    @DisplayName("예약자 이름이 비어있을 때 예외 발생을 테스트합니다.")
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" "})
    void validate_name(String name) {
        assertThatThrownBy(() ->
                User.builder()
                        .name(name)
                        .build())
                .isInstanceOf(RoomEscapeException.class)
                .hasMessage("이름은 비어있을 수 없습니다.");
    }
}
