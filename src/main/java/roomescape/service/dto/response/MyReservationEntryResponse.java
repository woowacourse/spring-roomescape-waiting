package roomescape.service.dto.response;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.domain.ReservationStatus;
import roomescape.domain.ReservationEntry;

public record MyReservationEntryResponse(Long id,
                                         LocalDate date,
                                         LocalTime time,
                                         String theme,
                                         String status) {

    public static MyReservationEntryResponse from(final ReservationEntry reservationEntry) {
        String status = "";
        if (reservationEntry.getStatus() == ReservationStatus.RESERVATION) {
            status = "예약";
        }
        if (reservationEntry.getStatus() == ReservationStatus.WAITING) {
            status = reservationEntry.getRank() + "번째 예약대기";
        }

        return new MyReservationEntryResponse(
                reservationEntry.getId(),
                reservationEntry.getDate(),
                reservationEntry.getTime().getStartAt(),
                reservationEntry.getTheme().getName(),
                status
        );
    }
}
