package roomescape.exception;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.time.temporal.ChronoUnit;
import roomescape.reservation.dto.ReservationSearchRequest;

public class DateValidator implements ConstraintValidator<ValidDate, ReservationSearchRequest> {

    public static final int LIMIT_DAY = 30;

    @Override
    public boolean isValid(ReservationSearchRequest request, ConstraintValidatorContext context) {
        System.out.println(request);
        return ChronoUnit.DAYS.between(request.dateFrom(), request.dateTo()) <= LIMIT_DAY;
    }
}
