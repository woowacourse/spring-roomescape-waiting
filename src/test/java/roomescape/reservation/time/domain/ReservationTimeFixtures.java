package roomescape.reservation.time.domain;

import jakarta.persistence.EntityManager;
import java.time.LocalTime;

public class ReservationTimeFixtures {

    private static final LocalTime DEFAULT_START_AT = LocalTime.of(10, 0);

    public static ReservationTime persistReservationTime(EntityManager entityManager, LocalTime startAt) {
        ReservationTime reservationTime = new ReservationTime(startAt);
        entityManager.persist(reservationTime);
        return reservationTime;
    }

    public static ReservationTime persistReservationTime(EntityManager entityManager) {
        return persistReservationTime(entityManager, DEFAULT_START_AT);
    }
}
