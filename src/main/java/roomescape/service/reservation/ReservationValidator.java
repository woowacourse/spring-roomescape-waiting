package roomescape.service.reservation;

import java.time.LocalDateTime;
import org.springframework.stereotype.Component;
import roomescape.domain.reservation.Reservation;
import roomescape.exception.ConflictException;
import roomescape.exception.ErrorCode;
import roomescape.exception.InvalidInputException;

@Component
public class ReservationValidator {

    public void validateReservationName(final String name) {
        if (name == null || name.isBlank()) {
            throw new InvalidInputException(ErrorCode.INVALID_INPUT, "예약자 이름은 필수입니다.");
        }

        if (name.length() >= 10) {
            throw new InvalidInputException(ErrorCode.INVALID_INPUT, "예약자 이름은 10자 미만이어야 합니다.");
        }
    }

    public void validateCreateRequest(final java.time.LocalDate date, final Long themeId, final Long timeId) {
        if (themeId == null) {
            throw new InvalidInputException(ErrorCode.INVALID_INPUT, "themeId는 필수입니다.");
        }

        if (timeId == null) {
            throw new InvalidInputException(ErrorCode.INVALID_INPUT, "timeId는 필수입니다.");
        }

        if (date == null) {
            throw new InvalidInputException(ErrorCode.INVALID_INPUT, "날짜는 필수입니다.");
        }
    }

    public void validateUpdateRequest(final java.time.LocalDate date, final Long timeId) {
        if (timeId == null) {
            throw new InvalidInputException(ErrorCode.INVALID_INPUT, "timeId는 필수입니다.");
        }

        if (date == null) {
            throw new InvalidInputException(ErrorCode.INVALID_INPUT, "날짜는 필수입니다.");
        }
    }

    public void validateReservable(final Reservation reservation) {
        if (reservation.isPastAt(LocalDateTime.now())) {
            throw new InvalidInputException(ErrorCode.RESERVATION_DATE_TIME_IN_PAST, "과거 날짜와 시간으로는 예약을 할 수 없습니다.");
        }
    }

    public void validateCancelable(final Reservation reservation) {
        if (reservation.isPastAt(LocalDateTime.now())) {
            throw new ConflictException(
                    ErrorCode.PAST_RESERVATION_CANNOT_BE_CANCELLED,
                    "이미 지난 예약은 취소할 수 없습니다."
            );
        }
    }

    public void validateUpdatable(final Reservation reservation) {
        if (reservation.isPastAt(LocalDateTime.now())) {
            throw new ConflictException(
                    ErrorCode.PAST_RESERVATION_CANNOT_BE_UPDATED,
                    "이미 지난 예약은 변경할 수 없습니다."
            );
        }
    }
}
