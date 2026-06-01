package roomescape.service.reservation;

import java.time.LocalDateTime;
import org.springframework.stereotype.Component;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationAvailabilityPolicy;
import roomescape.exception.ConflictException;
import roomescape.exception.ErrorCode;
import roomescape.exception.InvalidInputException;

@Component
public class ReservationValidator {
    private final ReservationAvailabilityPolicy reservationAvailabilityPolicy;

    public ReservationValidator(final ReservationAvailabilityPolicy reservationAvailabilityPolicy) {
        this.reservationAvailabilityPolicy = reservationAvailabilityPolicy;
    }

    public void validateLookupName(final String name) {
        if (name == null || name.isBlank()) {
            throw new InvalidInputException(ErrorCode.INVALID_INPUT, "예약자 이름은 필수입니다.");
        }

        if (name.length() >= 10) {
            throw new InvalidInputException(ErrorCode.INVALID_INPUT, "예약자 이름은 10자 미만이어야 합니다.");
        }
    }

    public void validateCreateReferenceIds(final Long themeId, final Long timeId) {
        if (themeId == null) {
            throw new InvalidInputException(ErrorCode.INVALID_INPUT, "themeId는 필수입니다.");
        }

        if (timeId == null) {
            throw new InvalidInputException(ErrorCode.INVALID_INPUT, "timeId는 필수입니다.");
        }
    }

    public void validateUpdateReferenceIds(final Long timeId) {
        if (timeId == null) {
            throw new InvalidInputException(ErrorCode.INVALID_INPUT, "timeId는 필수입니다.");
        }
    }

    public void validateCancelable(final Reservation reservation) {
        if (reservationAvailabilityPolicy.isPast(reservation, LocalDateTime.now())) {
            throw new ConflictException(
                    ErrorCode.PAST_RESERVATION_CANNOT_BE_CANCELLED,
                    "이미 지난 예약은 취소할 수 없습니다."
            );
        }
    }

    public void validateUpdatable(final Reservation reservation) {
        if (reservationAvailabilityPolicy.isPast(reservation, LocalDateTime.now())) {
            throw new ConflictException(
                    ErrorCode.PAST_RESERVATION_CANNOT_BE_UPDATED,
                    "이미 지난 예약은 변경할 수 없습니다."
            );
        }
    }
}
