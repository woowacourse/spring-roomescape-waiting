package roomescape.reservationtime.application;

import java.time.LocalTime;
import org.springframework.stereotype.Component;
import roomescape.reservationtime.domain.ReservationTime;

@Component
public class ReservationTimeAssembler {

    public ReservationTime assemble(LocalTime startAt) {
        return new ReservationTime(null, startAt);
    }
}
