package roomescape.support;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.domain.policy.ReservationPolicy;

public class AlwaysAllowPolicy implements ReservationPolicy {
    @Override
    public void validateCreatable(LocalDate date, LocalTime time) {
    }

    @Override
    public void validateCancellable(LocalDate date, LocalTime time) {
    }

    @Override
    public void validateUpdatable(LocalDate date, LocalTime time) {
    }

    @Override
    public void validateUpdateTarget(LocalDate date, LocalTime time) {
    }
}
