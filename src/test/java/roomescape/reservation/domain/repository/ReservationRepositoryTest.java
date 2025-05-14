package roomescape.reservation.domain.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.Reservation;
import roomescape.reservationTime.domain.ReservationTime;
import roomescape.theme.domain.Theme;

@ActiveProfiles("test")
@DataJpaTest
class ReservationRepositoryTest {
    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private ReservationRepository reservationRepository;

    @DisplayName("예약을 저장한다")
    @Test
    void save() {
        // given
        Member member = new Member("user", "user@example.com", "password");
        entityManager.persist(member);
        ReservationTime reservationTime = new ReservationTime(LocalTime.parse("10:00"));
        entityManager.persist(reservationTime);
        Theme theme = new Theme("roomescape", "timeAttack", "timeAttack.jpg");
        entityManager.persist(theme);
        LocalDate date = LocalDate.parse("2025-05-05");
        entityManager.flush();
        Reservation reservation = new Reservation(member, date, reservationTime, theme);

        // when
        reservationRepository.save(reservation);
        Iterable<Reservation> reservations = reservationRepository.findAll();

        // then
        assertThat(reservations).extracting(Reservation::getDate, Reservation::getMember, Reservation::getTime,
                        Reservation::getTheme)
                .containsExactlyInAnyOrder(tuple(date, member, reservationTime, theme));
    }
}
