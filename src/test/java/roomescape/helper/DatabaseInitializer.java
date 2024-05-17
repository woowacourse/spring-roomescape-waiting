package roomescape.helper;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDate;
import java.time.LocalTime;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Member;
import roomescape.domain.MemberRole;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationStatus;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;

@Component
@Transactional
public class DatabaseInitializer {
    @PersistenceContext
    private EntityManager entityManager;

    public void execute() {
        Member member = createMember();
        ReservationTime time = createTime();
        Theme theme = createTheme();
        Reservation reservation = createReservation(member, time, theme);
    }

    private Member createMember() {
        Member member = new Member("어드민", "admin@gmail.com", "1234", MemberRole.ADMIN);
        entityManager.persist(member);
        return member;
    }

    private ReservationTime createTime() {
        ReservationTime reservationTime = new ReservationTime(LocalTime.of(10, 0));
        entityManager.persist(reservationTime);
        return reservationTime;
    }


    private Theme createTheme() {
        Theme theme = new Theme("레벨2", "내용이다.", "https://www.naver.com/");
        entityManager.persist(theme);
        return theme;
    }

    private Reservation createReservation(Member member, ReservationTime time, Theme theme) {
        Reservation reservation = new Reservation(
                LocalDate.of(2024, 8, 5), member, time, theme, ReservationStatus.BOOKED);
        entityManager.persist(reservation);
        return reservation;
    }
}
