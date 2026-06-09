package roomescape.reservation.service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import org.springframework.stereotype.Component;
import roomescape.reservation.exception.InvalidReservationDateValueException;
import roomescape.time.exception.InvalidTimeStartAtValueException;

@Component
public class ExpiryValidator {

    private final Clock clock;

    public ExpiryValidator(Clock clock) {
        this.clock = clock;
    }

    public void validate(LocalDate date, LocalTime startAt) {
        LocalDate nowDate = LocalDate.now(clock);

        if (nowDate.isAfter(date)) {
            throw new InvalidReservationDateValueException();
        }

        if (nowDate.equals(date) && LocalTime.now(clock).isAfter(startAt)) {
            throw new InvalidTimeStartAtValueException();
        }
    }

}
