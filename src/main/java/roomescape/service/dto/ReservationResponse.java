package roomescape.service.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;
import java.time.LocalDate;
import java.time.LocalDateTime;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationSlot;
import roomescape.domain.reservation.ReservationStatus;
import roomescape.domain.reservation.Waiting;

public record ReservationResponse(
        Long id,
        MemberResponse member,
        @JsonFormat(shape = Shape.STRING, pattern = "yyyy-MM-dd", timezone = "Asia/Seoul")
        LocalDate date,
        ReservationTimeResponse time,
        ThemeResponse theme,
        ReservationStatus status
) {

    public static ReservationResponse createByWaiting(Waiting waiting) {
        ReservationSlot slot = waiting.getReservation().getSlot();

        return new ReservationResponse(
                waiting.getId(),
                MemberResponse.from(waiting.getMember()),
                slot.getDate(),
                new ReservationTimeResponse(slot.getTime()),
                new ThemeResponse(slot.getTheme()),
                ReservationStatus.WAIT
        );
    }

    public static ReservationResponse createByReservation(Reservation reservation) {
        return new ReservationResponse(
                reservation.getId(),
                MemberResponse.from(reservation.getMember()),
                reservation.getSlot().getDate(),
                new ReservationTimeResponse(reservation.getSlot().getTime()),
                new ThemeResponse(reservation.getSlot().getTheme()),
                ReservationStatus.BOOKED
        );
    }

    public LocalDateTime getDateTime() {
        return LocalDateTime.of(date, time.startAt());
    }
}
