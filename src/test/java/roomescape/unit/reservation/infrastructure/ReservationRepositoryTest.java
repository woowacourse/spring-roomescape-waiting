package roomescape.unit.reservation.infrastructure;

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
import roomescape.member.domain.Member;
import roomescape.member.domain.Role;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.Theme;
import roomescape.reservation.domain.TimeSlot;
import roomescape.reservation.infrastructure.ReservationRepository;

@DataJpaTest
class ReservationRepositoryTest {

    @Autowired
    private ReservationRepository reservationRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    void 날짜_시간_테마를_기준으로_예약을_조회한다() {
        // given
        Member member = Member.builder()
                .name("name1")
                .email("email1@domain.com")
                .password("password1")
                .role(Role.MEMBER).build();
        entityManager.persist(member);
        TimeSlot time = TimeSlot.builder()
                .startAt(LocalTime.of(9, 0)).build();
        entityManager.persist(time);
        Theme theme = Theme.builder()
                .name("theme1")
                .description("desc1")
                .thumbnail("thumb1").build();
        entityManager.persist(theme);
        Reservation reservation = Reservation.builder()
                .member(member)
                .date(LocalDate.of(2025, 1, 1))
                .timeSlot(time)
                .theme(theme).build();
        entityManager.persist(reservation);
        // when
        Optional<Reservation> findReservation = reservationRepository.findByDateAndTimeSlotAndTheme(
                LocalDate.of(2025, 1, 1), time, theme);
        // then
        assertThat(findReservation.isPresent()).isTrue();
        assertThat(findReservation.get().getDate()).isEqualTo(LocalDate.of(2025, 1, 1));
    }

    @Test
    void 테마id를_기준으로_예약을_조회한다() {
        // given
        Member member = Member.builder()
                .name("name1")
                .email("email1@domain.com")
                .password("password1")
                .role(Role.MEMBER).build();
        entityManager.persist(member);
        TimeSlot time = TimeSlot.builder()
                .startAt(LocalTime.of(9, 0)).build();
        entityManager.persist(time);
        Theme theme1 = Theme.builder()
                .name("theme1")
                .description("desc1")
                .thumbnail("thumb1").build();
        entityManager.persist(theme1);
        Theme theme2 = Theme.builder()
                .name("theme2")
                .description("desc2")
                .thumbnail("thumb2").build();
        entityManager.persist(theme2);
        entityManager.persist(
                Reservation.builder()
                        .member(member)
                        .date(LocalDate.of(2025, 1, 1))
                        .timeSlot(time)
                        .theme(theme1).build()
        );
        entityManager.persist(
                Reservation.builder()
                        .member(member)
                        .date(LocalDate.of(2025, 1, 1))
                        .timeSlot(time)
                        .theme(theme2).build()
        );
        entityManager.persist(
                Reservation.builder()
                        .member(member)
                        .date(LocalDate.of(2025, 1, 2))
                        .timeSlot(time)
                        .theme(theme1).build()
        );
        entityManager.persist(
                Reservation.builder()
                        .member(member)
                        .date(LocalDate.of(2025, 1, 2))
                        .timeSlot(time)
                        .theme(theme2).build()
        );
        // when
        List<Reservation> reservations = reservationRepository.findByThemeId(theme1.getId());
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
        Member member = Member.builder()
                .name("name1")
                .email("email1@domain.com")
                .password("password1")
                .role(Role.MEMBER).build();
        entityManager.persist(member);
        TimeSlot time = TimeSlot.builder()
                .startAt(LocalTime.of(9, 0)).build();
        entityManager.persist(time);
        Theme theme = Theme.builder()
                .name("theme1")
                .description("desc1")
                .thumbnail("thumb1").build();
        entityManager.persist(theme);
        entityManager.persist(
                Reservation.builder()
                        .member(member)
                        .date(LocalDate.of(2025, 1, 1))
                        .timeSlot(time)
                        .theme(theme).build()
        );
        entityManager.persist(
                Reservation.builder()
                        .member(member)
                        .date(LocalDate.of(2025, 1, 9))
                        .timeSlot(time)
                        .theme(theme).build()
        );
        entityManager.persist(
                Reservation.builder()
                        .member(member)
                        .date(LocalDate.of(2025, 1, 10))
                        .timeSlot(time)
                        .theme(theme).build()
        );
        // when
        List<Reservation> reservations = reservationRepository.findByDateBetween(
                LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 9)
        );
        // then
        assertThat(reservations).hasSize(2);
    }

    @Test
    void 테마와_날짜를_기준으로_예약을_조회한다() {
        // given
        Member member = Member.builder()
                .name("name1")
                .email("email1@domain.com")
                .password("password1")
                .role(Role.MEMBER).build();
        entityManager.persist(member);
        TimeSlot time = TimeSlot.builder()
                .startAt(LocalTime.of(9, 0)).build();
        entityManager.persist(time);
        Theme theme1 = Theme.builder()
                .name("theme1")
                .description("desc1")
                .thumbnail("thumb1").build();
        entityManager.persist(theme1);
        Theme theme2 = Theme.builder()
                .name("theme1")
                .description("desc1")
                .thumbnail("thumb1").build();
        entityManager.persist(theme2);
        entityManager.persist(
                Reservation.builder()
                        .member(member)
                        .date(LocalDate.of(2025, 1, 1))
                        .timeSlot(time)
                        .theme(theme1).build()
        );
        entityManager.persist(
                Reservation.builder()
                        .member(member)
                        .date(LocalDate.of(2025, 1, 2))
                        .timeSlot(time)
                        .theme(theme2).build()
        );
        entityManager.persist(
                Reservation.builder()
                        .member(member)
                        .date(LocalDate.of(2025, 1, 2))
                        .timeSlot(time)
                        .theme(theme1).build()
        );
        entityManager.persist(
                Reservation.builder()
                        .member(member)
                        .date(LocalDate.of(2025, 1, 1))
                        .timeSlot(time)
                        .theme(theme2).build()
        );
        // when
        List<Reservation> reservations = reservationRepository.findByDateAndTheme(LocalDate.of(2025, 1, 1), theme1);
        // then
        assertThat(reservations).hasSize(1);
    }

    @Test
    void 예약시간id를_기준으로_예약을_조회한다() {
        // given
        Member member = Member.builder()
                .name("name1")
                .email("email1@domain.com")
                .password("password1")
                .role(Role.MEMBER).build();
        entityManager.persist(member);
        TimeSlot time1 = TimeSlot.builder()
                .startAt(LocalTime.of(9, 0)).build();
        entityManager.persist(time1);
        TimeSlot time2 = TimeSlot.builder()
                .startAt(LocalTime.of(10, 0)).build();
        entityManager.persist(time2);
        Theme theme = Theme.builder()
                .name("theme1")
                .description("desc1")
                .thumbnail("thumb1").build();
        entityManager.persist(theme);
        entityManager.persist(
                Reservation.builder()
                        .member(member)
                        .date(LocalDate.of(2025, 1, 1))
                        .timeSlot(time1)
                        .theme(theme).build()
        );
        entityManager.persist(
                Reservation.builder()
                        .member(member)
                        .date(LocalDate.of(2025, 1, 1))
                        .timeSlot(time2)
                        .theme(theme).build()
        );
        // when
        List<Reservation> reservations = reservationRepository.findByTimeSlotId(time1.getId());
        // then
        assertThat(reservations).hasSize(1);
        assertThat(reservations.get(0).getTheme().getId()).isEqualTo(theme.getId());
    }

    @Test
    void 회원id를_기준으로_예약을_조회한다() {
        // given
        Member member1 = Member.builder()
                .name("name1")
                .email("email1@domain.com")
                .password("password1")
                .role(Role.MEMBER).build();
        entityManager.persist(member1);
        Member member2 = Member.builder()
                .name("name1")
                .email("email1@domain.com")
                .password("password1")
                .role(Role.MEMBER).build();
        entityManager.persist(member2);
        TimeSlot time = TimeSlot.builder()
                .startAt(LocalTime.of(9, 0)).build();
        entityManager.persist(time);
        Theme theme = Theme.builder()
                .name("theme1")
                .description("desc1")
                .thumbnail("thumb1").build();
        entityManager.persist(theme);
        entityManager.persist(
                Reservation.builder()
                        .member(member1)
                        .date(LocalDate.of(2025, 1, 1))
                        .timeSlot(time)
                        .theme(theme).build()
        );
        entityManager.persist(
                Reservation.builder()
                        .member(member2)
                        .date(LocalDate.of(2025, 1, 1))
                        .timeSlot(time)
                        .theme(theme).build()
        );
        // when
        List<Reservation> reservations = reservationRepository.findByMemberId(member1.getId());
        // then
        assertThat(reservations).hasSize(1);
        assertThat(reservations.get(0).getMember().getId()).isEqualTo(member1.getId());
    }
}