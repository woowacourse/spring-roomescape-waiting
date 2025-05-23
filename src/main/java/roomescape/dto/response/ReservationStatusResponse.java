package roomescape.dto.response;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationStatus;
import roomescape.domain.Waiting;

public record ReservationStatusResponse(
        Long id,
        LocalDate date,
        String status,
        ReservationTimeResponse time,
        ThemeResponse theme,
        MemberProfileResponse member
) {

    public ReservationStatusResponse(Reservation reservation) {
        this(
                reservation.getId(),
                reservation.getDate(),
                ReservationStatus.BOOKED.getDisplayName(),
                new ReservationTimeResponse(reservation.getReservationTime()),
                new ThemeResponse(reservation.getTheme()),
                new MemberProfileResponse(reservation.getMember())
        );
    }

    public ReservationStatusResponse(Waiting waiting) {
        this(
                waiting.getId(),
                waiting.getDate(),
                ReservationStatus.WAITING.getDisplayName(),
                new ReservationTimeResponse(waiting.getTime()),
                new ThemeResponse(waiting.getTheme()),
                new MemberProfileResponse(waiting.getMember())
        );
    }

    public static List<ReservationStatusResponse> createWithReservationAndWaiting(
            List<Reservation> reservations,
            List<Waiting> waitings
    ) {
        List<ReservationStatusResponse> responses = new ArrayList<>();
        reservations.forEach(reservation -> responses.add(new ReservationStatusResponse(reservation)));
        waitings.forEach(waiting -> responses.add(new ReservationStatusResponse(waiting)));
        return responses;
    }
}
