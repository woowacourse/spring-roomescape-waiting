package roomescape.dto.response;

import java.time.LocalDate;
import roomescape.model.Reservation;

public record ReservationResponseDto(
        Long id,
        MemberResponseDto member,
        LocalDate date,
        ReservationTimeResponseDto time,
        ThemeResponseDto theme
) {

    public ReservationResponseDto(Reservation reservationInfo) {
        this(
                reservationInfo.getId(),
                new MemberResponseDto(reservationInfo.getMember()),
                reservationInfo.getDate(),
                new ReservationTimeResponseDto(reservationInfo.getReservationTime()),
                new ThemeResponseDto(reservationInfo.getTheme())
        );
    }
}
