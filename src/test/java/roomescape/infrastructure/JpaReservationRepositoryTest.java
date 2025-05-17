package roomescape.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Role;
import roomescape.domain.Theme;

@DataJpaTest
class JpaReservationRepositoryTest {

    @Autowired
    private JpaReservationRepository jpaReservationRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    void 날짜_시간_테마를_기준으로_예약을_조회한다() {
        // given
        Member member = new Member(null, "name1", "email@domain.com", "password1", Role.MEMBER);
        entityManager.persist(member);
        ReservationTime time = ReservationTime.createWithoutId(LocalTime.of(9, 0));
        entityManager.persist(time);
        Theme theme = Theme.createWithoutId("theme1", "desc", "thumb");
        entityManager.persist(theme);
        entityManager.persist(Reservation.createWithoutId(member, LocalDate.of(2025, 1, 1), time, theme));
        // when
        Optional<Reservation> reservation = jpaReservationRepository.findByDateAndReservationTimeAndTheme(
                LocalDate.of(2025, 1, 1), time, theme);
        // then
        assertThat(reservation.isPresent()).isTrue();
        assertThat(reservation.get().getDate()).isEqualTo(LocalDate.of(2025, 1, 1));
    }

    @Test
    void 테마id를_기준으로_예약을_조회한다() {
        // given
        Member member = new Member(null, "name1", "email@domain.com", "password1", Role.MEMBER);
        entityManager.persist(member);
        ReservationTime time = ReservationTime.createWithoutId(LocalTime.of(9, 0));
        entityManager.persist(time);
        Theme theme1 = Theme.createWithoutId("theme1", "desc", "thumb");
        entityManager.persist(theme1);
        Theme theme2 = Theme.createWithoutId("theme2", "desc", "thumb");
        entityManager.persist(theme2);
        entityManager.persist(Reservation.createWithoutId(member, LocalDate.of(2025, 1, 1), time, theme1));
        entityManager.persist(Reservation.createWithoutId(member, LocalDate.of(2025, 1, 1), time, theme2));
        entityManager.persist(Reservation.createWithoutId(member, LocalDate.of(2025, 1, 2), time, theme1));
        entityManager.persist(Reservation.createWithoutId(member, LocalDate.of(2025, 1, 2), time, theme2));
        // when
        List<Reservation> reservations = jpaReservationRepository.findByThemeId(theme1.getId());
        // then
        SoftAssertions soft = new SoftAssertions();
        soft.assertThat(reservations).hasSize(2);
        soft.assertThat(reservations.get(0).getTheme().getName()).isEqualTo("theme1");
        soft.assertThat(reservations.get(1).getTheme().getName()).isEqualTo("theme1");
        soft.assertAll();
    }

    @Test
    void 날짜범위를_기준으로_예약을_조회한다() {
        // given
        Member member = new Member(null, "name1", "email@domain.com", "password1", Role.MEMBER);
        entityManager.persist(member);
        ReservationTime time = ReservationTime.createWithoutId(LocalTime.of(9, 0));
        entityManager.persist(time);
        Theme theme = Theme.createWithoutId("theme1", "desc", "thumb");
        entityManager.persist(theme);
        entityManager.persist(Reservation.createWithoutId(member, LocalDate.of(2025, 1, 1), time, theme));
        entityManager.persist(Reservation.createWithoutId(member, LocalDate.of(2025, 1, 9), time, theme));
        entityManager.persist(Reservation.createWithoutId(member, LocalDate.of(2025, 1, 10), time, theme));
        // when
        List<Reservation> reservations = jpaReservationRepository.findByDateBetween(
                LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 9)
        );
        // then
        assertThat(reservations).hasSize(2);
    }

    @Test
    void 테마와_날짜를_기준으로_예약을_조회한다() {
        // given
        Member member = new Member(null, "name1", "email@domain.com", "password1", Role.MEMBER);
        entityManager.persist(member);
        ReservationTime time = ReservationTime.createWithoutId(LocalTime.of(9, 0));
        entityManager.persist(time);
        Theme theme1 = Theme.createWithoutId("theme1", "desc", "thumb");
        entityManager.persist(theme1);
        Theme theme2 = Theme.createWithoutId("theme1", "desc", "thumb");
        entityManager.persist(theme2);
        entityManager.persist(Reservation.createWithoutId(member, LocalDate.of(2025, 1, 1), time, theme1));
        entityManager.persist(Reservation.createWithoutId(member, LocalDate.of(2025, 1, 2), time, theme2));
        entityManager.persist(Reservation.createWithoutId(member, LocalDate.of(2025, 1, 2), time, theme1));
        entityManager.persist(Reservation.createWithoutId(member, LocalDate.of(2025, 1, 1), time, theme2));
        // when
        List<Reservation> reservations = jpaReservationRepository.findByDateAndTheme(LocalDate.of(2025, 1, 1), theme1);
        // then
        assertThat(reservations).hasSize(1);
    }

    @Test
    void 예약시간id를_기준으로_예약을_조회한다() {
        // given
        Member member = new Member(null, "name1", "email@domain.com", "password1", Role.MEMBER);
        entityManager.persist(member);
        ReservationTime time1 = ReservationTime.createWithoutId(LocalTime.of(9, 0));
        entityManager.persist(time1);
        ReservationTime time2 = ReservationTime.createWithoutId(LocalTime.of(10, 0));
        entityManager.persist(time2);
        Theme theme = Theme.createWithoutId("theme1", "desc", "thumb");
        entityManager.persist(theme);
        entityManager.persist(Reservation.createWithoutId(member, LocalDate.of(2025, 1, 1), time1, theme));
        entityManager.persist(Reservation.createWithoutId(member, LocalDate.of(2025, 1, 1), time2, theme));
        // when
        List<Reservation> reservations = jpaReservationRepository.findByReservationTimeId(time1.getId());
        // then
        assertThat(reservations).hasSize(1);
        assertThat(reservations.get(0).getTheme().getId()).isEqualTo(theme.getId());
    }

    @Test
    void 회원id를_기준으로_예약을_조회한다() {
        // given
        Member member1 = new Member(null, "name1", "email@domain.com", "password1", Role.MEMBER);
        entityManager.persist(member1);
        Member member2 = new Member(null, "name1", "email@domain.com", "password1", Role.MEMBER);
        entityManager.persist(member2);
        ReservationTime time = ReservationTime.createWithoutId(LocalTime.of(9, 0));
        entityManager.persist(time);
        Theme theme = Theme.createWithoutId("theme1", "desc", "thumb");
        entityManager.persist(theme);
        entityManager.persist(Reservation.createWithoutId(member1, LocalDate.of(2025, 1, 1), time, theme));
        entityManager.persist(Reservation.createWithoutId(member2, LocalDate.of(2025, 1, 1), time, theme));
        // when
        List<Reservation> reservations = jpaReservationRepository.findByMemberId(member1.getId());
        // then
        assertThat(reservations).hasSize(1);
        assertThat(reservations.get(0).getMember().getId()).isEqualTo(member1.getId());
    }
}