package roomescape.reservation.infrastructure.db;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import roomescape.ReservationTestFixture;
import roomescape.member.model.Member;
import roomescape.reservation.infrastructure.db.dao.ReservationJpaRepository;
import roomescape.reservation.model.entity.Reservation;
import roomescape.reservation.model.entity.ReservationTheme;
import roomescape.reservation.model.entity.ReservationTime;
import roomescape.reservation.model.repository.dto.ReservationWithMember;
import roomescape.support.RepositoryTestSupport;

class ReservationJpaRepositoryTest extends RepositoryTestSupport {

    @Autowired
    private ReservationJpaRepository reservationJpaRepository;

    @PersistenceContext
    private EntityManager em;

    @DisplayName("예약과 함계 맴버 전체를 조회한다")
    @Test
    void findAllWithMembers() {
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

        List<ReservationWithMember> reservationsWithMember = reservationJpaRepository.findAllWithMember();

        SoftAssertions.assertSoftly(softAssertions -> {
            softAssertions.assertThat(reservationsWithMember).hasSize(2);
            softAssertions.assertThat(reservationsWithMember.getFirst().memberId()).isEqualTo(member.getId());
        });
    }

    @DisplayName("예약 id로 예약과 맴버를 함꼐 조회한다")
    @Test
    void findAllWithMember() {
        Member member = ReservationTestFixture.getUserFixture();
        ReservationTheme reservationTheme = ReservationTestFixture.getReservationThemeFixture();
        ReservationTime time = ReservationTestFixture.getReservationTimeFixture();
        Reservation reservation = ReservationTestFixture.getReservationFixture(
            LocalDate.now().plusDays(1), member, time, reservationTheme);
        Reservation reservation1 = ReservationTestFixture.getReservationFixture(
            LocalDate.now().plusDays(2), member, time, reservationTheme);
        em.persist(member);
        em.persist(time);
        em.persist(reservationTheme);
        em.persist(reservation);
        em.persist(reservation1);
        em.flush();
        List<Reservation> reservations = reservationJpaRepository.findAll();
        Optional<ReservationWithMember> reservationWithMember = reservationJpaRepository.findWithMemberById(
            reservation1.getId());
        SoftAssertions.assertSoftly(softAssertions -> {
            softAssertions.assertThat(reservationWithMember.get().id()).isEqualTo(reservation1.getId());
            softAssertions.assertThat(reservationWithMember.get().memberId()).isEqualTo(member.getId());
        });
    }


}
