package roomescape.reservation.domain;

import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import roomescape.member.domain.Member;
import roomescape.reservation.time.domain.ReservationTime;
import roomescape.theme.domain.Theme;

public class ReservationFixtures {

    public static Reservation persistReservedReservation(
            EntityManager entityManager,
            Theme theme, Member member,
            ReservationTime time,
            LocalDate date
    ) {
        Reservation reservation = Reservation.createReserved(member, theme, date, time);
        entityManager.persist(reservation);
        return reservation;
    }
}
