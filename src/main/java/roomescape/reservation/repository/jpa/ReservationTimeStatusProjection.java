package roomescape.reservation.repository.jpa;

import java.time.LocalTime;

public interface ReservationTimeStatusProjection {
    Long getId();
    LocalTime getStartAt();
    Boolean getReserved();
}
