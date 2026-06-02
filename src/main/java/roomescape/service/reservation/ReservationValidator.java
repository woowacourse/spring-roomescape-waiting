package roomescape.service.reservation;

import java.time.Clock;
import java.time.LocalDateTime;
import org.springframework.stereotype.Component;
import roomescape.domain.ReserverName;
import roomescape.domain.reservation.Reservation;
import roomescape.exception.ConflictException;
import roomescape.exception.ErrorCode;
import roomescape.exception.InvalidInputException;

@Component
public class ReservationValidator {

    private final Clock clock;

    public ReservationValidator(final Clock clock) {
        this.clock = clock;
    }

    public void validateLookupName(final String name) {
        ReserverName.from(name);
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

    public void validateReservable(final Reservation reservation) {
        if (reservation.isPastAt(LocalDateTime.now(clock))) {
            throw new InvalidInputException(ErrorCode.RESERVATION_DATE_TIME_IN_PAST, "과거 날짜와 시간으로는 예약을 할 수 없습니다.");
        }
    }

    public void validateCancelable(final Reservation reservation) {
        if (reservation.isPastAt(LocalDateTime.now(clock))) {
            throw new ConflictException(
                    ErrorCode.PAST_RESERVATION_CANNOT_BE_CANCELLED,
                    "이미 지난 예약은 취소할 수 없습니다."
            );
        }
    }

    public void validateUpdatable(final Reservation reservation) {
        if (reservation.isPastAt(LocalDateTime.now(clock))) {
            throw new ConflictException(
                    ErrorCode.PAST_RESERVATION_CANNOT_BE_UPDATED,
                    "이미 지난 예약은 변경할 수 없습니다."
            );
        }
    }
}
