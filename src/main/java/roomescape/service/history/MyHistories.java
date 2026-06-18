package roomescape.service.history;

import java.util.List;
import roomescape.repository.history.MyHistory;

public class MyHistories {
    private static final String RESERVATION_STATUS = "RESERVATION";
    private static final String WAITING_STATUS = "WAITING";

    private final List<MyHistory> histories;

    public MyHistories(final List<MyHistory> histories) {
        this.histories = histories;
    }

    public List<Long> waitingReservationIds() {
        return histories.stream()
                .filter(history -> WAITING_STATUS.equals(history.status()))
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
        if (RESERVATION_STATUS.equals(history.status())) {
            return 0;
        }

        return waitingLines.sequenceOf(history.reservationId(), history.waitingId());
    }
}
