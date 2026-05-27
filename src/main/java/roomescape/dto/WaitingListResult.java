package roomescape.dto;

import roomescape.domain.WaitingList;

import java.time.LocalDate;

public record WaitingListResult(
        Long id,
        String name,
        LocalDate date,
        Long timeId,
        Long themeId
) {
    public static WaitingListResult from(WaitingList waitingList) {
        return new WaitingListResult(
                waitingList.getId(),
                waitingList.getName(),
                waitingList.getDate(),
                waitingList.getReservationTime().getId(),
                waitingList.getTheme().getId());
    }
}
