package roomescape.reservation.controller.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;
import roomescape.member.controller.dto.MemberResponse;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.MemberReservation;
import roomescape.reservation.domain.Reservation;

public record ReservationResponse(
        @JsonProperty("id")
        long memberReservationId,
        MemberResponse member,
        LocalDate date,
        ReservationTimeResponse time,
        ThemeResponse theme) {
    public static ReservationResponse from(long memberReservationId, Reservation reservation, Member member) {
        return new ReservationResponse(
                memberReservationId,
                MemberResponse.from(member),
                reservation.getDate(),
                ReservationTimeResponse.from(reservation.getTime()),
                ThemeResponse.from(reservation.getTheme())
        );
    }

    public static ReservationResponse from(MemberReservation memberReservation) {
        return new ReservationResponse(
                memberReservation.getId(),
                MemberResponse.from(memberReservation.getMember()),
                memberReservation.getReservation().getDate(),
                ReservationTimeResponse.from(memberReservation.getReservation().getTime()),
                ThemeResponse.from(memberReservation.getReservation().getTheme())
        );
    }
}
