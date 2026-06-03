package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalTime;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import roomescape.exception.BusinessException;
import roomescape.exception.ErrorCode;

class ReservationTimeTest {

    @Nested
    class 생성 {

        @Test
        void 성공() {
            LocalTime startAt = LocalTime.of(10, 0);
            LocalTime endAt = LocalTime.of(11, 0);

            ReservationTime time = ReservationTime.create(startAt, endAt);

            assertThat(time.getId()).isNull();
            assertThat(time.getStartAt()).isEqualTo(startAt);
            assertThat(time.getEndAt()).isEqualTo(endAt);
        }

        @Test
        void startAt이_null이면_예외발생() {
            assertThatThrownBy(() -> ReservationTime.create(null, LocalTime.of(11, 0)))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.START_TIME_NULL);
        }

        @Test
        void endAt이_null이면_예외발생() {
            assertThatThrownBy(() -> ReservationTime.create(LocalTime.of(10, 0), null))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.END_TIME_NULL);
        }

        @Test
        void withId로_id_부여() {
            ReservationTime time = ReservationTime.create(LocalTime.of(10, 0), LocalTime.of(11, 0));

            ReservationTime withId = time.withId(5L);

            assertThat(withId.getId()).isEqualTo(5L);
            assertThat(withId.getStartAt()).isEqualTo(time.getStartAt());
        }

        @Test
        void withId에_null_전달시_예외발생() {
            ReservationTime time = ReservationTime.create(LocalTime.of(10, 0), LocalTime.of(11, 0));

            assertThatThrownBy(() -> time.withId(null))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.TIME_ID_NULL);
        }
    }

    @Nested
    class isBefore {

        @Test
        void 과거_시간이면_true() {
            ReservationTime time = ReservationTime.createWithId(1L, LocalTime.now().minusHours(1), LocalTime.now());
            assertThat(time.isBefore()).isTrue();
        }

        @Test
        void 미래_시간이면_false() {
            ReservationTime time = ReservationTime.createWithId(1L, LocalTime.now().plusHours(1), LocalTime.now().plusHours(2));
            assertThat(time.isBefore()).isFalse();
        }
    }
}
