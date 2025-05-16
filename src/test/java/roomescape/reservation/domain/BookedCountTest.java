package roomescape.reservation.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

class BookedCountTest {

    @Test
    @DisplayName("예약 횟수는 음수가 불가능하다")
    void throwWhenNegativeCountValue() {
        // when
        // then
        assertAll(()->{
            assertThatCode(() -> BookedCount.from(0))
                    .doesNotThrowAnyException();

            assertThatCode(() -> BookedCount.from(1))
                    .doesNotThrowAnyException();

            assertThatThrownBy(() -> BookedCount.from(-1))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("BookedCount must not be negative: -1");
        });
    }
}
