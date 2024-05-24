package roomescape.reservation.controller.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;
import roomescape.member.controller.dto.MemberResponse;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationSlot;

public record ReservationResponse(
        @JsonProperty("id")
        long memberReservationId,
        MemberResponse member,
        LocalDate date,
        ReservationTimeResponse time,
        ThemeResponse theme) {
    public static ReservationResponse from(long memberReservationId, ReservationSlot reservationSlot, Member member) {
        return new ReservationResponse(
                memberReservationId,
                MemberResponse.from(member),
                reservationSlot.getDate(),
                ReservationTimeResponse.from(reservationSlot.getTime()),
                ThemeResponse.from(reservationSlot.getTheme())
        );
    }

    public static ReservationResponse from(Reservation memberReservation) {
        ReservationSlot reservationSlot = memberReservation.getReservationSlot();
        return new ReservationResponse(
                memberReservation.getId(),
                MemberResponse.from(memberReservation.getMember()),
                reservationSlot.getDate(),
                ReservationTimeResponse.from(reservationSlot.getTime()),
                ThemeResponse.from(reservationSlot.getTheme())
        );
    }
}
