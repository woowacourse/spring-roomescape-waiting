package roomescape.wating.domain;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.domain.Theme;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.*;

class WaitingTest {

    private static final LocalDateTime NOW = LocalDateTime.now(Clock.fixed(
            LocalDate.of(2026, 5, 8)
                    .atTime(10, 30)
                    .atZone(ZoneId.of("Asia/Seoul"))
                    .toInstant(),
            ZoneId.of("Asia/Seoul")
    ));

    @Test
    void 현재_시간_이전으로_대기를_등록할_수_없다() {
        Assertions.assertThatThrownBy(() -> Waiting.create(
                "코로구",
                NOW.toLocalDate(),
                ReservationTime.of(1L, NOW.minusHours(1L).toLocalTime()),
                Theme.of(1L, "샘플 테마", "테스트용 샘플", "http:~"),
                NOW
        ));
    }
}
