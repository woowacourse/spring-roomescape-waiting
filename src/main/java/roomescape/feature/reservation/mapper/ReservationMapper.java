package roomescape.feature.reservation.mapper;

import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import roomescape.feature.reservation.dto.command.ReservationCreateCommand;
import roomescape.feature.reservation.dto.command.ReservationUpdateCommand;
import roomescape.feature.reservation.dto.request.ReservationCreateRequestDto;
import roomescape.feature.reservation.dto.request.ReservationUpdateRequestDto;
import roomescape.feature.reservation.dto.response.ReservationCancelResponseDto;
import roomescape.feature.reservation.dto.response.ReservationCreateResponseDto;
import roomescape.feature.reservation.dto.response.ReservationEditableStatus;
import roomescape.feature.reservation.dto.response.ReservationResponseDto;
import roomescape.feature.payment.domain.Payment;
import roomescape.feature.reservation.domain.Reservation;
import roomescape.feature.reservation.domain.ReservationStatus;
import roomescape.feature.reservation.domain.ReserverName;
import roomescape.feature.theme.mapper.ThemeMapper;
import roomescape.feature.time.mapper.TimeMapper;
import roomescape.global.domain.EntityStatus;

@Component
@RequiredArgsConstructor
public final class ReservationMapper {

    private final TimeMapper timeMapper;
    private final ThemeMapper themeMapper;

    public ReservationCreateCommand toCreateCommand(ReservationCreateRequestDto requestDto) {
        return new ReservationCreateCommand(new ReserverName(requestDto.name()), requestDto.date(), requestDto.timeId(),
            requestDto.themeId());
    }

    public ReservationUpdateCommand toUpdateCommand(ReservationUpdateRequestDto requestDto) {
        return new ReservationUpdateCommand(new ReserverName(requestDto.name()), requestDto.date(), requestDto.timeId(),
            requestDto.themeId());
    }

    public ReservationResponseDto toResponseDto(Reservation reservation, Integer waitingNumber, Payment payment) {
        ReservationEditableStatus status = getStatus(reservation);
        String orderId = payment == null ? null : payment.getOrderId();
        String paymentKey = payment == null ? null : payment.getPaymentKey();

        return new ReservationResponseDto(reservation.getId(), reservation.getName().value(), reservation.getDate(),
            timeMapper.toReservationResponseDto(reservation.getTime()),
            themeMapper.toReservationResponseDto(reservation.getTheme()), status, status.getMessage(), waitingNumber,
            reservation.getOrderStatus(), orderId, paymentKey, reservation.getAmount());
    }

    private ReservationEditableStatus getStatus(Reservation reservation) {
        if (reservation.getStatus() == ReservationStatus.DELETED) {
            return ReservationEditableStatus.DELETED;
        }

        if (reservation.getStatus() == ReservationStatus.CANCELED) {
            return ReservationEditableStatus.CANCELED;
        }

        if (reservation.getDate().isBefore(LocalDate.now())) {
            return ReservationEditableStatus.LOCKED;
        }

        if (reservation.getStatus() == ReservationStatus.WAITING) {
            return ReservationEditableStatus.WAITING;
        }

        if (reservation.getTime().getStatus() == EntityStatus.DELETED || reservation.getTheme().getStatus() == EntityStatus.DELETED) {
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
