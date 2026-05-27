package roomescape.wating.domain;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import org.junit.jupiter.api.Test;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.domain.Theme;
import roomescape.wating.domain.exception.PastDateTimeWaitingException;

class WaitingTest {

    private static final LocalDateTime NOW = LocalDateTime.now(Clock.fixed(
        LocalDate.of(2026, 5, 8)
            .atTime(10, 30)
            .atZone(ZoneId.of("Asia/Seoul"))
            .toInstant(),
        ZoneId.of("Asia/Seoul")
    ));

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
