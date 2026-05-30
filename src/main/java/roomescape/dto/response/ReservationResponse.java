package roomescape.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import roomescape.domain.Reservation;
import roomescape.domain.Slot;
import roomescape.domain.WaitingWithRank;

import java.time.LocalDate;

public record ReservationResponse(
        Long id,
        String name,

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "Asia/Seoul")
        LocalDate date,

        TimeInfo time,
        ThemeInfo theme,

        ReservationStatus status,
        Integer rank
) {
    public static ReservationResponse from(Reservation reservation) {
        Slot slot = reservation.slot();
        return new ReservationResponse(
                reservation.id(),
                reservation.owner().name(),
                slot.date(),
                TimeInfo.from(slot.time()),
                ThemeInfo.from(slot.theme()),
                ReservationStatus.RESERVED,
                null
        );
    }

    public static ReservationResponse from(WaitingWithRank waiting) {
        Slot slot = waiting.slot();
        return new ReservationResponse(
                waiting.id(),
                waiting.name(),
                slot.date(),
                TimeInfo.from(slot.time()),
                ThemeInfo.from(slot.theme()),
                ReservationStatus.WAITING,
                waiting.rank()
        );
    }
}
