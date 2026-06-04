package roomescape.dto;

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
    public static WaitingListResult from(final WaitingList waitingList, final int waitingOrder) {
        return new WaitingListResult(
                waitingList.getId(),
                waitingList.getName(),
                waitingList.getReservationDate().date(),
                waitingList.getReservationTime().getId(),
                waitingList.getTheme().getId(),
                waitingOrder,
                ReservationStatus.WAITING_LIST
        );
    }
}
