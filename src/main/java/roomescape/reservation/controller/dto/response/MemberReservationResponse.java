package roomescape.reservation.controller.dto.response;

import java.time.LocalDate;
import java.util.List;
import roomescape.member.controller.dto.response.MemberNameResponse;
import roomescape.reservation.domain.Reservation;

public record MemberReservationResponse(
        long id,
        MemberNameResponse member,
        LocalDate date,
        ReservationTimeResponse time,
        ThemeResponse theme,
        String status
) {

    public static MemberReservationResponse from(final Reservation reservation) {
        return new MemberReservationResponse(
                reservation.getId(),
                new MemberNameResponse(reservation.getMember().getNameValue()),
                reservation.getDate(),
                ReservationTimeResponse.from(reservation.getTime()),
                ThemeResponse.from(reservation.getTheme()),
                reservation.getStatus().getValue()
        );
    }

    public static List<MemberReservationResponse> list(final List<Reservation> reservations) {
        return reservations.stream()
                .map(MemberReservationResponse::from)
                .toList();
    }
}
