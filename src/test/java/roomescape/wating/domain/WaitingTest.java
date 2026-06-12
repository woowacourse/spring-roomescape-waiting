package roomescape.wating.domain;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.domain.Theme;

import java.time.LocalDateTime;

class WaitingTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 5, 8, 10, 30);

    @Test
    @DisplayName("현재 시간 이전으로 대기를 등록할 수 없다")
    void cannotRegisterWaitingBeforeNow() {
        Assertions.assertThatThrownBy(() -> Waiting.create(
                "코로구",
                "customer@example.com",
NOW.toLocalDate(),
                ReservationTime.of(1L, NOW.minusHours(1L).toLocalTime()),
                Theme.of(1L, "샘플 테마", "테스트용 샘플", "http:~"),
                NOW
        ));
    }
}
