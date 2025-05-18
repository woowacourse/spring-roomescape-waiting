package roomescape;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;

@Component
@Transactional
public class DBHelper {

    @PersistenceContext
    private EntityManager em;

    public Long insertReservation(Reservation reservation) {
        if(reservation.getMember().getId() == null) {
            em.persist(reservation.getMember());
        }
        if(reservation.getTime().getId() == null) {
            em.persist(reservation.getTime());
        }
        if(reservation.getTheme().getId() == null) {
            em.persist(reservation.getTheme());
        }

        em.persist(reservation);

        return reservation.getId();
    }

    public void insertMember(Member member) {
        em.persist(member);
    }

    public void insertTime(ReservationTime reservationTime) {
        em.persist(reservationTime);
    }

    public void insertTheme(Theme theme) {
        em.persist(theme);
    }

    public void prepareForReservation(Member member, ReservationTime time, Theme theme) {
        insertMember(member);
        insertTime(time);
        insertTheme(theme);
    }
}
