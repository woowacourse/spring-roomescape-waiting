package roomescape.service.history;

import java.util.List;
import roomescape.domain.history.ReservationHistoryStatus;
import roomescape.repository.history.MyHistory;

public class MyHistories {

    private final List<MyHistory> histories;

    public MyHistories(final List<MyHistory> histories) {
        this.histories = histories;
    }

    public List<Long> waitingReservationIds() {
        return histories.stream()
                .filter(history -> history.status() == ReservationHistoryStatus.WAITING)
                .map(MyHistory::reservationId)
                .distinct()
                .toList();
    }

    public List<MyHistoryResult> toResults(final MyWaitingLines waitingLines) {
        return histories.stream()
                .map(history -> toResult(history, waitingLines))
                .toList();
    }

    private MyHistoryResult toResult(final MyHistory history, final MyWaitingLines waitingLines) {
        return new MyHistoryResult(
                history.reservationId(),
                history.waitingId(),
                history.status(),
                history.name(),
                history.date(),
                history.theme(),
                history.time(),
                resolveSequence(history, waitingLines)
        );
    }

    private Integer resolveSequence(final MyHistory history, final MyWaitingLines waitingLines) {
        if (history.status() == ReservationHistoryStatus.RESERVATION) {
            return 0;
        }

        return waitingLines.sequenceOf(history.reservationId(), history.waitingId());
    }
}
