package roomescape.reservation.domain;

import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import roomescape.member.domain.Member;
import roomescape.reservation.time.domain.ReservationTime;
import roomescape.theme.domain.Theme;

public class ReservationFixtures {

    public static Reservation persistReservedReservation(
            EntityManager entityManager,
            Member member, Theme theme,
            LocalDate date,
            ReservationTime reservationTime
    ) {
        Reservation reservation = Reservation.createReserved(member, theme, date, reservationTime);
        entityManager.persist(reservation);
        return reservation;
    }

    public static Reservation persistWaitingReservation(
            EntityManager entityManager,
            Member member, Theme theme,
            LocalDate date, ReservationTime reservationTime
    ) {
        Reservation reservation = Reservation.createWaiting(member, theme, date, reservationTime);
        entityManager.persist(reservation);
        return reservation;
    }
}
