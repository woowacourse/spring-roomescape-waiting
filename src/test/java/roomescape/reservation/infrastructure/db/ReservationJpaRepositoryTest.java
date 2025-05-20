package roomescape.reservation.infrastructure.db;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.ReservationTestFixture;
import roomescape.member.model.Member;
import roomescape.reservation.infrastructure.db.dao.ReservationJpaRepository;
import roomescape.reservation.model.entity.Reservation;
import roomescape.reservation.model.entity.ReservationTheme;
import roomescape.reservation.model.entity.ReservationTime;
import roomescape.support.RepositoryTestSupport;

@Disabled
class ReservationJpaRepositoryTest extends RepositoryTestSupport {

    @Autowired
    private ReservationJpaRepository reservationJpaRepository;

    @PersistenceContext
    private EntityManager em;

    @DisplayName("예약과 함계 맴버 전체를 조회한다")
    @Test
    void findAll() {
        Member member = ReservationTestFixture.getUserFixture();
        ReservationTheme reservationTheme = ReservationTestFixture.getReservationThemeFixture();
        ReservationTime time = ReservationTestFixture.getReservationTimeFixture();
        Reservation reservation = ReservationTestFixture.getReservationFixture(
            LocalDate.now().plusDays(1), member, time, reservationTheme);
        Reservation reservation1 = ReservationTestFixture.getReservationFixture(
            LocalDate.now().plusDays(2), member, time, reservationTheme);
        em.persist(member);
        em.persist(reservationTheme);
        em.persist(time);
        em.persist(reservation);
        em.persist(reservation1);

        List<Reservation> reservations = reservationJpaRepository.findAll();

        SoftAssertions.assertSoftly(softAssertions -> {
            softAssertions.assertThat(reservations).hasSize(2);
            softAssertions.assertThat(reservations.getFirst().getMember())
                .isEqualTo(member.getId());
        });
    }
}
