package roomescape.history;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.theme.Theme;
import roomescape.repository.history.MyHistory;
import roomescape.repository.history.MyWaitingOrder;
import roomescape.service.history.MyHistories;
import roomescape.service.history.MyHistoryResult;
import roomescape.service.history.MyWaitingLines;

class MyHistoriesTest {

    @Test
    @DisplayName("대기 상태인 내역의 예약 ID만 중복 없이 추린다")
    void waitingReservationIds() {
        MyHistories histories = new MyHistories(List.of(
                history(1L, null, "RESERVATION"),
                history(2L, 1L, "WAITING"),
                history(2L, 2L, "WAITING"),
                history(3L, 3L, "WAITING")
        ));

        assertThat(histories.waitingReservationIds()).containsExactly(2L, 3L);
    }

    @Test
    @DisplayName("예약 내역과 대기 순번을 조회 결과로 묶는다")
    void toResults() {
        MyHistories histories = new MyHistories(List.of(
                history(1L, null, "RESERVATION"),
                history(2L, 2L, "WAITING")
        ));
        MyWaitingLines waitingLines = MyWaitingLines.from(List.of(
                waitingOrder(2L, 1L, "2026-08-05T12:00:00"),
                waitingOrder(2L, 2L, "2026-08-05T12:01:00")
        ));

        List<MyHistoryResult> results = histories.toResults(waitingLines);

        assertThat(results).extracting(MyHistoryResult::sequence)
                .containsExactly(0, 2);
    }

    private MyHistory history(
            final Long reservationId,
            final Long waitingId,
            final String status
    ) {
        return new MyHistory(
                reservationId,
                waitingId,
                status,
                "아루",
                LocalDate.parse("2026-08-06"),
                Theme.of(1L, "미술관의 밤", "추리 테마", "https://example.com/theme.png"),
                ReservationTime.of(1L, LocalTime.parse("10:00")),
                LocalDateTime.parse("2026-08-05T12:00:00")
        );
    }

    private MyWaitingOrder waitingOrder(
            final Long reservationId,
            final Long waitingId,
            final String requestedAt
    ) {
        return new MyWaitingOrder(
                reservationId,
                reservationId,
                waitingId,
                LocalDateTime.parse(requestedAt)
        );
    }
}
