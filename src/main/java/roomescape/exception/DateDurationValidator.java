package roomescape.exception;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.time.temporal.ChronoUnit;
import roomescape.reservation.dto.request.ReservationSearchRequest;

public class DateDurationValidator implements ConstraintValidator<ValidDateDuration, ReservationSearchRequest> {

    private int limitDay;

    @Override
    public void initialize(final ValidDateDuration constraintAnnotation) {
        this.limitDay = constraintAnnotation.days();
    }

    @Override
    public boolean isValid(ReservationSearchRequest request, ConstraintValidatorContext context) {
        return ChronoUnit.DAYS.between(request.dateFrom(), request.dateTo()) <= limitDay;
    }
}
