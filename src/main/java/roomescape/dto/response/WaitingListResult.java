package roomescape.dto.response;

import roomescape.domain.ReservationStatus;
import roomescape.domain.WaitingList;

import java.time.LocalDate;

public record WaitingListResult(
        Long id,
        String name,
        LocalDate date,
        Long timeId,
        Long themeId,
        int waitingOrder,
        ReservationStatus status
) {
    public static WaitingListResult from(WaitingList waitingList, int waitingOrder) {
        return new WaitingListResult(
                waitingList.getId(),
                waitingList.getName(),
                waitingList.getReservationDate().getDate(),
                waitingList.getReservationTime().getId(),
                waitingList.getTheme().getId(),
                waitingOrder,
                ReservationStatus.WAITING_LIST
        );
    }
}
