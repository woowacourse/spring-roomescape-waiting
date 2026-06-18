package roomescape.domain.reservation.mapper;

import org.springframework.stereotype.Component;
import roomescape.domain.reservation.dto.command.ReservationCreateCommand;
import roomescape.domain.reservation.dto.command.ReservationUpdateCommand;
import roomescape.domain.reservation.dto.request.ReservationCreateRequestDto;
import roomescape.domain.reservation.dto.request.ReservationUpdateRequestDto;
import roomescape.domain.reservation.dto.response.ReservationCancelResponseDto;
import roomescape.domain.reservation.dto.response.ReservationCreateResponseDto;
import roomescape.domain.reservation.dto.response.ReservationResponseDto;
import roomescape.domain.reservation.entity.Reservation;
import roomescape.domain.reservation.entity.ReservationEditableStatus;
import roomescape.domain.theme.mapper.ThemeMapper;
import roomescape.domain.time.mapper.TimeMapper;

@Component
public final class ReservationMapper {

    private final TimeMapper timeMapper;
    private final ThemeMapper themeMapper;

    public ReservationMapper(TimeMapper timeMapper, ThemeMapper themeMapper) {
        this.timeMapper = timeMapper;
        this.themeMapper = themeMapper;
    }

    public ReservationCreateCommand toCreateCommand(ReservationCreateRequestDto requestDto) {
        return new ReservationCreateCommand(requestDto.name(), requestDto.date(), requestDto.timeId(),
            requestDto.themeId());
    }

    public ReservationUpdateCommand toUpdateCommand(ReservationUpdateRequestDto requestDto) {
        return new ReservationUpdateCommand(requestDto.date(), requestDto.timeId(), requestDto.themeId(),
            requestDto.version());
    }

    public ReservationResponseDto toResponseDto(
        Reservation reservation,
        ReservationEditableStatus status,
        Integer waitingNumber
    ) {
        return new ReservationResponseDto(reservation.getId(), reservation.getName(), reservation.getDate(),
            timeMapper.toReservationResponseDto(reservation.getTime()),
            themeMapper.toReservationResponseDto(reservation.getTheme()), status, status.getMessage(), waitingNumber,
            reservation.getVersion());
    }

    public ReservationCreateResponseDto toCreateResponseDto(Reservation reservation) {
        return new ReservationCreateResponseDto(reservation.getId(), reservation.getName(),
            reservation.getDate(), reservation.getTime().getId(), reservation.getTheme().getId());
    }

    public ReservationCancelResponseDto toCancelResponseDto(Reservation reservation) {
        return new ReservationCancelResponseDto(reservation.getId(), reservation.getName(),
            reservation.getDate(), reservation.getTime().getId(), reservation.getTheme().getId());
    }
}
