package roomescape.domain.reservation.mapper;

import java.time.Clock;
import java.time.LocalDate;
import org.springframework.stereotype.Component;
import roomescape.domain.reservation.dto.command.ReservationCreateCommand;
import roomescape.domain.reservation.dto.command.ReservationUpdateCommand;
import roomescape.domain.reservation.dto.request.ReservationCreateRequestDto;
import roomescape.domain.reservation.dto.request.ReservationUpdateRequestDto;
import roomescape.domain.reservation.dto.response.ReservationCancelResponseDto;
import roomescape.domain.reservation.dto.response.ReservationCreateResponseDto;
import roomescape.domain.reservation.dto.response.ReservationEditableStatus;
import roomescape.domain.reservation.dto.response.ReservationResponseDto;
import roomescape.domain.reservation.entity.Reservation;
import roomescape.domain.reservation.entity.ReservationStatus;
import roomescape.domain.reservation.vo.ReserverName;
import roomescape.domain.theme.mapper.ThemeMapper;
import roomescape.domain.time.mapper.TimeMapper;

@Component
public final class ReservationMapper {

    private final TimeMapper timeMapper;
    private final ThemeMapper themeMapper;
    private final Clock clock;

    public ReservationMapper(TimeMapper timeMapper, ThemeMapper themeMapper, Clock clock) {
        this.timeMapper = timeMapper;
        this.themeMapper = themeMapper;
        this.clock = clock;
    }

    public ReservationCreateCommand toCreateCommand(ReservationCreateRequestDto requestDto) {
        return new ReservationCreateCommand(new ReserverName(requestDto.name()), requestDto.date(), requestDto.timeId(),
            requestDto.themeId());
    }

    public ReservationUpdateCommand toUpdateCommand(ReservationUpdateRequestDto requestDto) {
        return new ReservationUpdateCommand(new ReserverName(requestDto.name()), requestDto.date(), requestDto.timeId(),
            requestDto.themeId());
    }

    public ReservationResponseDto toResponseDto(Reservation reservation, Integer waitingNumber) {
        ReservationEditableStatus status = getStatus(reservation);

        return new ReservationResponseDto(reservation.getId(), reservation.getName().value(), reservation.getDate(),
            timeMapper.toReservationResponseDto(reservation.getTime()),
            themeMapper.toReservationResponseDto(reservation.getTheme()), status, status.getMessage(), waitingNumber);
    }

    private ReservationEditableStatus getStatus(Reservation reservation) {
        if (reservation.getStatus() == ReservationStatus.CANCELED) {
            return ReservationEditableStatus.CANCELED;
        }

        if (reservation.getDate().isBefore(LocalDate.now(clock))) {
            return ReservationEditableStatus.LOCKED;
        }

        if (reservation.getStatus() == ReservationStatus.WAITING) {
            return ReservationEditableStatus.WAITING;
        }

        if (reservation.getTime().isDeleted() || reservation.getTheme().isDeleted()) {
            return ReservationEditableStatus.EDIT_RECOMMENDED;
        }

        return ReservationEditableStatus.EDITABLE;
    }

    public ReservationCreateResponseDto toCreateResponseDto(Reservation reservation) {
        return new ReservationCreateResponseDto(reservation.getId(), reservation.getName().value(),
            reservation.getDate(), reservation.getTime().getId(), reservation.getTheme().getId());
    }

    public ReservationCancelResponseDto toCancelResponseDto(Reservation reservation) {
        return new ReservationCancelResponseDto(reservation.getId(), reservation.getName().value(),
            reservation.getDate(), reservation.getTime().getId(), reservation.getTheme().getId());
    }
}
