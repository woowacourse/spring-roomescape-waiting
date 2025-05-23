package roomescape.reservation.time.domain;

import jakarta.persistence.EntityManager;
import java.time.LocalTime;

public class ReservationTimeFixtures {

    public static ReservationTime createAndPersistReservationTime(EntityManager entityManager) {
        ReservationTime reservationTime = new ReservationTime(LocalTime.MIDNIGHT);
        entityManager.persist(reservationTime);
        return reservationTime;
    }
}
