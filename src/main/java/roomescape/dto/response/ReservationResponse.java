package roomescape.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import roomescape.domain.Reservation;
import roomescape.domain.Slot;
import roomescape.domain.WaitingWithRank;

import java.time.LocalDate;
import java.time.LocalTime;

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
                reservation.username(),
                slot.date(),
                new TimeInfo(slot.time().id(), slot.time().startAt()),
                new ThemeInfo(
                        slot.theme().id(),
                        slot.theme().name(),
                        slot.theme().thumbnailUrl(),
                        slot.theme().description()),
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
                new TimeInfo(slot.time().id(), slot.time().startAt()),
                new ThemeInfo(
                        slot.theme().id(),
                        slot.theme().name(),
                        slot.theme().thumbnailUrl(),
                        slot.theme().description()),
                "예약대기",
                waiting.rank()
        );
    }

    private record TimeInfo(
            Long id,

            @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm", timezone = "Asia/Seoul")
            LocalTime startAt) {

    }

    private record ThemeInfo(Long id,
                             String name,
                             String thumbnailUrl,
                             String description
    ) {
    }
}
