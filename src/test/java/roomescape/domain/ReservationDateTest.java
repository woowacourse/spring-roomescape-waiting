package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class ReservationDateTest {

    @DisplayName("생성 테스트")
    @Test
    void create() {
        assertThatCode(() -> new ReservationDate("2024-06-01"))
            .doesNotThrowAnyException();
    }

    @DisplayName("잘못된 날짜 형식으로 생성하면 예외가 발생한다.")
    @ParameterizedTest
    @ValueSource(strings = {"024-04-04", "2024-13-10", "2025-04-31"})
    void createByIllegalData(String input) {
        assertThatThrownBy(() -> new ReservationDate(input))
            .isInstanceOf(IllegalArgumentException.class);
    }
}
