package roomescape.time.domain;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static roomescape.time.exception.ReservationTimeErrorInformation.ID_IS_NULL;
import static roomescape.time.exception.ReservationTimeErrorInformation.START_AT_IS_NULL;

import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import roomescape.time.exception.ReservationTimeException;

class ReservationTimeTest {


    @Nested
    @DisplayName("validateStartAt 메서드는")
    class ValidateTest {


        @Test
        @DisplayName("startAt이 null이면 예외가 발생한다")
        void 실패() {
            assertThatThrownBy(() -> ReservationTime.load(1L, null, false))
                .isInstanceOf(ReservationTimeException.class)
                .hasMessage(START_AT_IS_NULL.getMessage());
        }
    }

    @Nested
    @DisplayName("load 메서드는")
    class LoadTest {


        @Test
        @DisplayName("id가 null이면 예외가 발생한다")
        void 실패() {
            // given
            Long nullId = null;

            // when & then
            assertThatThrownBy(() -> ReservationTime.load(nullId, LocalTime.of(10, 0), false))
                .isInstanceOf(ReservationTimeException.class)
                .hasMessage(ID_IS_NULL.getMessage());
        }
    }
}
