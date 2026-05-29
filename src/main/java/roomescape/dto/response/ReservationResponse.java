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

        String status,
        Integer rank
) {
    public static ReservationResponse from(Reservation reservation) {
        Slot slot = reservation.slot();
        return new ReservationResponse(
                reservation.id(),
                reservation.name(),
                slot.date(),
                TimeInfo.from(slot.time()),
                ThemeInfo.from(slot.theme()),
                "예약",
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
                "예약대기",
                waiting.rank()
        );
    }
}
