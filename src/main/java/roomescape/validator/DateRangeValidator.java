package roomescape.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.time.temporal.ChronoUnit;
import roomescape.reservation.dto.ReservationSearchConditionRequest;

public class DateRangeValidator implements ConstraintValidator<ValidDateRange, ReservationSearchConditionRequest> {

    public static final int LIMIT_DAY = 30;

    @Override
    public boolean isValid(ReservationSearchConditionRequest request, ConstraintValidatorContext context) {
        return ChronoUnit.DAYS.between(request.dateFrom(), request.dateTo()) <= LIMIT_DAY;
    }
}
