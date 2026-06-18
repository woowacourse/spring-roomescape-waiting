package roomescape.controller.dto.response;

import java.time.LocalDate;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import roomescape.domain.reservation.RankedReservation;
import roomescape.domain.reservation.Reservation;

@Getter
@RequiredArgsConstructor
public class ReservationResponse {
    private final long id;
    private final String name;
    private final LocalDate date;
    private final String state;
    private final int rank;
    private final ReservationTimeResponse time;
    private final ThemeResponse theme;

    public static ReservationResponse from(RankedReservation rankedReservation) {
        Reservation reservation = rankedReservation.getReservation();
        return new ReservationResponse(reservation.getId(), reservation.getName().getValue(),
                reservation.getSlot().getDate().getValue(),
                rankedReservation.getReservation().getStatus().getKoreanName(),
                rankedReservation.getRank().getValue(),
                ReservationTimeResponse.from(reservation.getSlot().getTime()),
                ThemeResponse.from(reservation.getSlot().getTheme()));
    }
}
