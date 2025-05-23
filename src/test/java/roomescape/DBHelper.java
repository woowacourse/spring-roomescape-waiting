package roomescape;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.Waiting;

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
        em.flush();

        return reservation.getId();
    }

    public Waiting insertWaiting(Waiting waiting) {
        if(waiting.getMember().getId() == null) {
            em.persist(waiting.getMember());
        }
        if(waiting.getTime().getId() == null) {
            em.persist(waiting.getTime());
        }
        if(waiting.getTheme().getId() == null) {
            em.persist(waiting.getTheme());
        }

        em.persist(waiting);
        em.flush();

        return waiting;
    }

    public void insertMember(Member member) {
        em.persist(member);
        em.flush();
    }

    public void insertTime(ReservationTime reservationTime) {
        em.persist(reservationTime);
        em.flush();
    }

    public void insertTheme(Theme theme) {
        em.persist(theme);
        em.flush();
    }

    public void prepareForBooking(Member member, ReservationTime time, Theme theme) {
        insertMember(member);
        insertTime(time);
        insertTheme(theme);
        em.flush();
    }
}
