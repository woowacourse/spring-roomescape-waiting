package roomescape.reservation.controller.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;
import roomescape.member.controller.dto.MemberResponse;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationSlot;

public record ReservationResponse(
        @JsonProperty("id")
        long reservationId,
        MemberResponse member,
        LocalDate date,
        ReservationTimeResponse time,
        ThemeResponse theme) {
    public static ReservationResponse from(long reservationId, ReservationSlot reservationSlot, Member member) {
        return new ReservationResponse(
                reservationId,
                MemberResponse.from(member),
                reservationSlot.getDate(),
                ReservationTimeResponse.from(reservationSlot.getTime()),
                ThemeResponse.from(reservationSlot.getTheme())
        );
    }

    public static ReservationResponse from(Reservation reservation) {
        ReservationSlot reservationSlot = reservation.getReservationSlot();
        return new ReservationResponse(
                reservation.getId(),
                MemberResponse.from(reservation.getMember()),
                reservationSlot.getDate(),
                ReservationTimeResponse.from(reservationSlot.getTime()),
                ThemeResponse.from(reservationSlot.getTheme())
        );
    }
}
