package roomescape.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.time.temporal.ChronoUnit;
import roomescape.reservation.dto.ReservationSearchCondRequest;

public class DateRangeValidator implements ConstraintValidator<ValidDateRange, ReservationSearchCondRequest> {

    public static final int LIMIT_DAY = 30;

    @Override
    public boolean isValid(ReservationSearchCondRequest request, ConstraintValidatorContext context) {
        return ChronoUnit.DAYS.between(request.dateFrom(), request.dateTo()) <= LIMIT_DAY;
    }
}
