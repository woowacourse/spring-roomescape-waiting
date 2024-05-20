package roomescape.reservation.presentation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import roomescape.reservation.domain.ReservationStatus;

import java.util.Arrays;
import java.util.List;

public class ReservationStatusFormatValidator implements ConstraintValidator<ReservationStatusFormat, String> {

    @Override
    public void initialize(ReservationStatusFormat constraintAnnotation) {
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext constraintValidatorContext) {
        if (value == null || value.isBlank()) {
            return true;
        }
        List<String> reservationStatusNames = Arrays.stream(ReservationStatus.values())
                .map(ReservationStatus::name)
                .toList();
        return reservationStatusNames.stream()
                .anyMatch(reservationStatusName -> isConvertible(reservationStatusName, value));
    }

    public boolean isConvertible(String reservationStatusName, String value) {
        return value.trim().equalsIgnoreCase(reservationStatusName);
    }
}
