package roomescape.application.dto;

import java.time.LocalDate;
import java.util.List;
import roomescape.domain.Reservation;

public record ReservationDto(
        long id,
        MemberDto member,
        ThemeDto theme,
        LocalDate date,
        TimeDto time
) {
    public static ReservationDto from(Reservation reservation) {
        return new ReservationDto(
                reservation.getId(),
                MemberDto.from(reservation.getMember()),
                ThemeDto.from(reservation.getTheme()),
                reservation.getReservationDate(),
                TimeDto.from(reservation.getReservationTime())
        );
    }

    public static List<ReservationDto> from(List<Reservation> reservations) {
        return reservations.stream()
                .map(ReservationDto::from)
                .toList();
    }
}
