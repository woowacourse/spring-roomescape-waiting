package roomescape.business.model.entity;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import roomescape.exception.business.InvalidCreateArgumentException;

import java.time.LocalTime;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

class ReservationTimeTest {

    @Nested
    class 생성_테스트 {

        @ParameterizedTest
        @CsvSource({"09:59", "23:01"})
        void 예약_가능한_시간이_아닐_때_예약하면_예외가_발생한다(String timeStrValue) {
            assertThatThrownBy(() -> new ReservationTime(LocalTime.parse(timeStrValue)))
                    .isInstanceOf(InvalidCreateArgumentException.class);
        }
    }
}
