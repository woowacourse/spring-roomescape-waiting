package roomescape.unit.reservation.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import roomescape.member.domain.Member;
import roomescape.member.domain.Role;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.Theme;
import roomescape.reservation.domain.TimeSlot;
import roomescape.reservation.dto.request.ReservationCondition;
import roomescape.reservation.infrastructure.ReservationRepositoryImpl;

@DataJpaTest
class ReservationRepositoryImplTest {

    @Autowired
    private ReservationRepositoryImpl reservationRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    void 테마를_기준으로_예약을_조회한다() {
        // given
        Member member = Member.builder()
                .name("name1")
                .email("email1@domain.com")
                .password("password1")
                .role(Role.MEMBER).build();
        TimeSlot time = TimeSlot.builder()
                .startAt(LocalTime.of(9, 0)).build();
        Theme theme1 = Theme.builder()
                .name("theme1")
                .description("desc1")
                .thumbnail("thumb1").build();
        Theme theme2 = Theme.builder()
                .name("theme2")
                .description("desc2")
                .thumbnail("thumb2").build();
        Reservation reservation1 = Reservation.builder()
                .member(member)
                .reservationTime(new ReservationTime(LocalDate.of(2025, 1, 1), time))
                .theme(theme1).build();
        Reservation reservation2 = Reservation.builder()
                .member(member)
                .reservationTime(new ReservationTime(LocalDate.of(2025, 1, 1), time))
                .theme(theme2).build();
        entityManager.persist(time);
        entityManager.persist(member);
        entityManager.persist(theme1);
        entityManager.persist(theme2);
        entityManager.persist(reservation1);
        entityManager.persist(reservation2);
        ReservationCondition condition = new ReservationCondition(theme1.getId(), null, null, null);
        // when
        List<Reservation> reservations = reservationRepository.findByCondition(condition);
        // then
        assertThat(reservations).hasSize(1);
        assertThat(reservations.get(0)).isEqualTo(reservation1);
    }

    @Test
    void 회원을_기준으로_예약을_조회한다() {
        // given
        Member member1 = Member.builder()
                .name("name1")
                .email("email1@domain.com")
                .password("password1")
                .role(Role.MEMBER)
                .build();
        Member member2 = Member.builder()
                .name("name2")
                .email("email2@domain.com")
                .password("password2")
                .role(Role.MEMBER)
                .build();
        TimeSlot time = TimeSlot.builder()
                .startAt(LocalTime.of(9, 0))
                .build();
        Theme theme = Theme.builder()
                .name("theme1")
                .description("desc1")
                .thumbnail("thumb1")
                .build();
        Reservation reservation1 = Reservation.builder()
                .member(member1)
                .reservationTime(new ReservationTime(LocalDate.of(2025, 1, 1), time))
                .theme(theme)
                .build();
        Reservation reservation2 = Reservation.builder()
                .member(member2)
                .reservationTime(new ReservationTime(LocalDate.of(2025, 1, 1), time))
                .theme(theme)
                .build();
        entityManager.persist(time);
        entityManager.persist(member1);
        entityManager.persist(member2);
        entityManager.persist(theme);
        entityManager.persist(reservation1);
        entityManager.persist(reservation2);
        ReservationCondition condition = new ReservationCondition(null, member1.getId(), null, null);
        // when
        List<Reservation> reservations = reservationRepository.findByCondition(condition);
        // then
        assertThat(reservations).hasSize(1);
        assertThat(reservations.get(0)).isEqualTo(reservation1);
    }

    @Test
    void 시작일_기준으로_예약을_조회한다() {
        // given
        Member member = Member.builder()
                .name("name1")
                .email("email1@domain.com")
                .password("password1")
                .role(Role.MEMBER)
                .build();
        TimeSlot time = TimeSlot.builder()
                .startAt(LocalTime.of(9, 0))
                .build();
        Theme theme = Theme.builder()
                .name("theme1")
                .description("desc1")
                .thumbnail("thumb1")
                .build();
        Reservation reservation1 = Reservation.builder()
                .member(member)
                .reservationTime(new ReservationTime(LocalDate.of(2025, 1, 1), time))
                .theme(theme)
                .build();
        Reservation reservation2 = Reservation.builder()
                .member(member)
                .reservationTime(new ReservationTime(LocalDate.of(2025, 1, 2), time))
                .theme(theme)
                .build();
        entityManager.persist(time);
        entityManager.persist(member);
        entityManager.persist(theme);
        entityManager.persist(reservation1);
        entityManager.persist(reservation2);
        ReservationCondition condition = new ReservationCondition(null, null, LocalDate.of(2025, 1, 2), null);
        // when
        List<Reservation> reservations = reservationRepository.findByCondition(condition);
        // then
        assertThat(reservations).hasSize(1);
        assertThat(reservations.get(0)).isEqualTo(reservation2);
    }

    @Test
    void 종료일_기준으로_예약을_조회한다() {
        // given
        Member member = Member.builder()
                .name("name1")
                .email("email1@domain.com")
                .password("password1")
                .role(Role.MEMBER)
                .build();
        TimeSlot time = TimeSlot.builder()
                .startAt(LocalTime.of(9, 0))
                .build();
        Theme theme = Theme.builder()
                .name("theme1")
                .description("desc1")
                .thumbnail("thumb1")
                .build();
        Reservation reservation1 = Reservation.builder()
                .member(member)
                .reservationTime(new ReservationTime(LocalDate.of(2025, 1, 1), time))
                .theme(theme)
                .build();
        Reservation reservation2 = Reservation.builder()
                .member(member)
                .reservationTime(new ReservationTime(LocalDate.of(2025, 1, 2), time))
                .theme(theme)
                .build();
        entityManager.persist(time);
        entityManager.persist(member);
        entityManager.persist(theme);
        entityManager.persist(reservation1);
        entityManager.persist(reservation2);
        ReservationCondition condition = new ReservationCondition(null, null, null, LocalDate.of(2025, 1, 1));
        // when
        List<Reservation> reservations = reservationRepository.findByCondition(condition);
        // then
        assertThat(reservations).hasSize(1);
        assertThat(reservations.get(0)).isEqualTo(reservation1);
    }

    @Test
    void 시작일과_종료일_기준으로_예약을_조회한다() {
        // given
        Member member = Member.builder()
                .name("name1")
                .email("email1@domain.com")
                .password("password1")
                .role(Role.MEMBER)
                .build();
        TimeSlot time = TimeSlot.builder()
                .startAt(LocalTime.of(9, 0))
                .build();
        Theme theme = Theme.builder()
                .name("theme1")
                .description("desc1")
                .thumbnail("thumb1")
                .build();
        Reservation reservation1 = Reservation.builder()
                .member(member)
                .reservationTime(new ReservationTime(LocalDate.of(2025, 1, 1), time))
                .theme(theme)
                .build();
        Reservation reservation2 = Reservation.builder()
                .member(member)
                .reservationTime(new ReservationTime(LocalDate.of(2025, 1, 2), time))
                .theme(theme)
                .build();
        entityManager.persist(time);
        entityManager.persist(member);
        entityManager.persist(theme);
        entityManager.persist(reservation1);
        entityManager.persist(reservation2);
        ReservationCondition condition = new ReservationCondition(null, null, LocalDate.of(2025, 1, 1),
                LocalDate.of(2025, 1, 2));
        // when
        List<Reservation> reservations = reservationRepository.findByCondition(condition);
        // then
        assertThat(reservations).hasSize(2);
        assertThat(reservations.get(0)).isEqualTo(reservation1);
    }

    @Test
    void 회원과_테마를_기준으로_예약을_조회한다() {
        // given
        Member member1 = Member.builder()
                .name("name1")
                .email("email1@domain.com")
                .password("password1")
                .role(Role.MEMBER)
                .build();
        Member member2 = Member.builder()
                .name("name2")
                .email("email2@domain.com")
                .password("password2")
                .role(Role.MEMBER)
                .build();
        TimeSlot time = TimeSlot.builder()
                .startAt(LocalTime.of(9, 0))
                .build();
        Theme theme1 = Theme.builder()
                .name("theme1")
                .description("desc1")
                .thumbnail("thumb1")
                .build();
        Theme theme2 = Theme.builder()
                .name("theme2")
                .description("desc2")
                .thumbnail("thumb2")
                .build();
        Reservation reservation1 = Reservation.builder()
                .member(member1)
                .reservationTime(new ReservationTime(LocalDate.of(2025, 1, 1), time))
                .theme(theme1)
                .build();
        Reservation reservation2 = Reservation.builder()
                .member(member1)
                .reservationTime(new ReservationTime(LocalDate.of(2025, 1, 1), time))
                .theme(theme2)
                .build();
        Reservation reservation3 = Reservation.builder()
                .member(member2)
                .reservationTime(new ReservationTime(LocalDate.of(2025, 1, 1), time))
                .theme(theme1)
                .build();
        Reservation reservation4 = Reservation.builder()
                .member(member2)
                .reservationTime(new ReservationTime(LocalDate.of(2025, 1, 1), time))
                .theme(theme2)
                .build();
        entityManager.persist(time);
        entityManager.persist(member1);
        entityManager.persist(member2);
        entityManager.persist(theme1);
        entityManager.persist(theme2);
        entityManager.persist(reservation1);
        entityManager.persist(reservation2);
        entityManager.persist(reservation3);
        entityManager.persist(reservation4);
        ReservationCondition condition = new ReservationCondition(theme1.getId(), member1.getId(), null, null);
        // when
        List<Reservation> reservations = reservationRepository.findByCondition(condition);
        // then
        assertThat(reservations).hasSize(1);
        assertThat(reservations.get(0)).isEqualTo(reservation1);
    }
}