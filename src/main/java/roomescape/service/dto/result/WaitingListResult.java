package roomescape.service.dto.result;

import roomescape.domain.WaitingList;
import roomescape.service.dto.ReservationTimeStatus;

import java.time.LocalDate;

public record WaitingListResult(
        Long id,
        String name,
        LocalDate date,
        Long timeId,
        Long themeId,
        int waitingOrder,
        ReservationTimeStatus status
) {
    public static WaitingListResult from(final WaitingList waitingList, final int waitingOrder) {
        return new WaitingListResult(
                waitingList.getId(),
                waitingList.getName(),
                waitingList.getReservationDate().date(),
                waitingList.getReservationTime().getId(),
                waitingList.getTheme().getId(),
                waitingOrder,
                ReservationTimeStatus.WAITING_LIST
        );
    }
}
