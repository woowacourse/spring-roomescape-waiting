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
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.Waiting;

@Component
@Transactional
public class DatabaseInitializer {
    @PersistenceContext
    private final EntityManager entityManager;

    public DatabaseInitializer(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public void execute() {
        Member admin = createAdmin();
        Member user = createUser();
        ReservationTime time = createTime();
        Theme theme = createTheme();
        createReservation(admin, user, time, theme);
        createWaiting(admin, user, time, theme);
    }

    private Member createAdmin() {
        Member member = new Member("어드민", "admin@email.com", "password", MemberRole.ADMIN);
        entityManager.persist(member);
        return member;
    }

    private Member createUser() {
        Member member = new Member("유저", "user@email.com", "password", MemberRole.USER);
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

    private void createReservation(Member admin, Member user, ReservationTime time, Theme theme) {
        Reservation reservation1 = new Reservation(LocalDate.of(2024, 8, 5), user, time, theme);
        Reservation reservation2 = new Reservation(LocalDate.of(2024, 8, 6), admin, time, theme);
        entityManager.persist(reservation1);
        entityManager.persist(reservation2);
    }

    private void createWaiting(Member admin, Member user, ReservationTime time, Theme theme) {
        Waiting waiting = new Waiting(LocalDate.of(2024, 8, 6), user, time, theme);
        entityManager.persist(waiting);
    }
}
