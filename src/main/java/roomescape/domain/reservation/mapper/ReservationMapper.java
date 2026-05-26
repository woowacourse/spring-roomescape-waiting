package roomescape.domain.reservation.mapper;

import roomescape.domain.reservation.dto.response.ReservationByNameResponseDto;
import roomescape.domain.reservation.dto.response.ReservationCancelResponseDto;
import roomescape.domain.reservation.dto.response.ReservationCreateResponseDto;
import roomescape.domain.reservation.dto.response.ReservationResponseDto;
import roomescape.domain.reservation.dto.response.ReservationStatus;
import roomescape.domain.reservation.entity.Reservation;
import roomescape.domain.reservation.entity.Status;
import roomescape.domain.theme.mapper.ThemeMapper;
import roomescape.domain.time.mapper.TimeMapper;

public final class ReservationMapper {

    private ReservationMapper() {

    }

    public static ReservationResponseDto toResponseDto(Reservation reservation) {
        return new ReservationResponseDto(reservation.getId(), reservation.getName(), reservation.getDate(),
            TimeMapper.toResponseDto(reservation.getTime()), ThemeMapper.toResponseDto(reservation.getTheme()),
            reservation.getStatus() == Status.CANCELED);
    }

    public static ReservationByNameResponseDto toByNameResponseDto(Reservation reservation, ReservationStatus status) {
        return new ReservationByNameResponseDto(reservation.getId(), reservation.getName(), reservation.getDate(),
            TimeMapper.toReservationResponseDto(reservation.getTime()),
            ThemeMapper.toReservationResponseDto(reservation.getTheme()), status, status.getMessage());
    }

    public static ReservationCreateResponseDto toCreateResponseDto(Reservation reservation) {
        return new ReservationCreateResponseDto(reservation.getId(), reservation.getName(), reservation.getDate(),
            reservation.getTime().getId(), reservation.getTheme().getId());
    }

    public static ReservationCancelResponseDto toCancelResponseDto(Reservation reservation) {
        return new ReservationCancelResponseDto(reservation.getId(), reservation.getName(), reservation.getDate(),
            reservation.getTime().getId(), reservation.getTheme().getId());
    }
}
