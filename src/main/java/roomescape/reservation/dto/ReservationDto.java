package roomescape.reservation.dto;

import roomescape.member.dto.MemberDto;
import roomescape.reservation.model.Reservation;
import roomescape.reservation.model.ReservationDate;
import roomescape.reservation.model.ReservationStatus;

public record ReservationDto(
        Long id,
        ReservationStatus status,
        ReservationDate date,
        ReservationTimeDto time,
        ThemeDto theme,
        MemberDto member
) {
    public static ReservationDto from(Reservation reservation) {
        return new ReservationDto(
                reservation.getId(),
                reservation.getStatus(),
                reservation.getDate(),
                ReservationTimeDto.from(reservation.getTime()),
                ThemeDto.from(reservation.getTheme()),
                MemberDto.from(reservation.getMember())
        );
    }
}
