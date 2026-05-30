package roomescape.waiting.domain;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.domain.Theme;
import roomescape.waiting.domain.exception.PastDateTimeWaitingException;

class WaitingTest {

    private static final LocalDateTime NOW = LocalDateTime.now(Clock.fixed(
        LocalDate.of(2026, 5, 8)
            .atTime(10, 30)
            .atZone(ZoneId.of("Asia/Seoul"))
            .toInstant(),
        ZoneId.of("Asia/Seoul")
    ));

    @Nested
    @DisplayName("필드가 null인 경우 객체 생성 시 예외가 발생한다")
    class RequireNonNull {

        @Test
        void 예약_날짜가_null인_경우_예외가_발생한다() {
            assertThatThrownBy(() -> Waiting.create(
                "name",
                null,
                ReservationTime.of(1L, NOW.toLocalTime()),
                Theme.of(1L, "name", "description", "url"),
                NOW
            )).isInstanceOf(NullPointerException.class);
        }

        @Test
        void 예약_시간이_null인_경우_예외가_발생한다() {
            assertThatThrownBy(() -> Waiting.create(
                "name",
                NOW.toLocalDate(),
                null,
                Theme.of(1L, "name", "description", "url"),
                NOW
            )).isInstanceOf(NullPointerException.class);
        }

        @Test
        void 테마가_null인_경우_예외가_발생한다() {
            assertThatThrownBy(() -> Waiting.create(
                "name",
                NOW.toLocalDate(),
                ReservationTime.of(1L, NOW.toLocalTime()),
                null,
                NOW
            )).isInstanceOf(NullPointerException.class);
        }
    }

    @Test
    void 현재_시간_이전으로_대기를_등록하면_예외가_발생한다() {
        // given
        final LocalDate today = NOW.toLocalDate();
        final LocalTime beforeOneHour = NOW.minusHours(1L).toLocalTime();

        // when & then
        assertThatThrownBy(() -> Waiting.create(
            "코로구",
            today,
            ReservationTime.of(1L, beforeOneHour),
            Theme.of(1L, "샘플 테마", "테스트용 샘플", "http:~"),
            NOW)
        ).isInstanceOf(PastDateTimeWaitingException.class);
    }
}
