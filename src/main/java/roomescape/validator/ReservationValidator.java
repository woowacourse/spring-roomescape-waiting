package roomescape.validator;

import java.time.LocalDateTime;
import roomescape.domain.Reservation;

public interface ReservationValidator {

    void validateCreate(Reservation reservation, LocalDateTime now);

    void validateDelete(Reservation reservation, LocalDateTime now);
}
