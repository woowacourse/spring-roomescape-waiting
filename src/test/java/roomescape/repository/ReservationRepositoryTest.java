package roomescape.repository;

import static org.assertj.core.api.Assertions.assertThat;

import static roomescape.model.Member.createMember;
import static roomescape.model.Reservation.createAcceptReservation;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;

import roomescape.model.Member;
import roomescape.model.Reservation;
import roomescape.model.ReservationTime;
import roomescape.model.Theme;
import roomescape.service.dto.MemberReservation;

@DataJpaTest
@Sql("/init-data.sql")
class ReservationRepositoryTest {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private ReservationRepository reservationRepository;

    @DisplayName("모든 예약을 조회한다.")
    @Test
    void should_get_all_reservations() {
        LocalDate day = LocalDate.of(2024, 5, 15);
        ReservationTime time1 = new ReservationTime(LocalTime.of(10, 0));
        ReservationTime time2 = new ReservationTime(LocalTime.of(11, 0));
        Theme theme = new Theme("무빈테마", "무빈테마설명", "무빈테마썸네일");
        Member member = createMember("무빈", "movin@email.com", "password");

        entityManager.persist(time1);
        entityManager.persist(time2);
        entityManager.persist(theme);
        entityManager.persist(member);

        Reservation reservation1 = createAcceptReservation(day, time1, theme, member);
        Reservation reservation2 = createAcceptReservation(day, time2, theme, member);

        entityManager.persist(reservation1);
        entityManager.persist(reservation2);

        List<Reservation> reservations = reservationRepository.findAll();
        assertThat(reservations).hasSize(2);
    }

    @DisplayName("특정 날짜와 테마에 해당하는 시간을 조회한다.")
    @Test
    void should_search_reservation_by_condition() {
        LocalDate day = LocalDate.of(2024, 5, 15);
        ReservationTime time1 = new ReservationTime(LocalTime.of(10, 0));
        ReservationTime time2 = new ReservationTime(LocalTime.of(11, 0));
        Theme theme = new Theme("무빈테마", "무빈테마설명", "무빈테마썸네일");
        Member member = createMember("무빈", "movin@email.com", "password");

        entityManager.persist(time1);
        entityManager.persist(time2);
        entityManager.persist(theme);
        entityManager.persist(member);

        Reservation reservation1 = createAcceptReservation(day, time1, theme, member);
        Reservation reservation2 = createAcceptReservation(day, time2, theme, member);

        entityManager.persist(reservation1);
        entityManager.persist(reservation2);

        List<Reservation> reservations = reservationRepository.findAllByDateAndTheme(day, theme);

        assertThat(reservations).hasSize(2);
    }

    @DisplayName("조회한 예약에 예약 시간이 존재한다.")
    @Test
    void should_get_reservation_times() {
        LocalDate day = LocalDate.of(2024, 5, 15);
        ReservationTime time1 = new ReservationTime(LocalTime.of(10, 0));
        ReservationTime time2 = new ReservationTime(LocalTime.of(11, 0));
        Theme theme = new Theme("무빈테마", "무빈테마설명", "무빈테마썸네일");
        Member member = createMember("무빈", "movin@email.com", "password");

        entityManager.persist(time1);
        entityManager.persist(time2);
        entityManager.persist(theme);
        entityManager.persist(member);

        Reservation reservation1 = createAcceptReservation(day, time1, theme, member);
        Reservation reservation2 = createAcceptReservation(day, time2, theme, member);

        entityManager.persist(reservation1);
        entityManager.persist(reservation2);

        List<Reservation> reservations = reservationRepository.findAll();
        assertThat(reservations.get(0).getTime().getStartAt()).isEqualTo(LocalTime.of(10, 0));
    }

    @DisplayName("예약을 추가한다")
    @Test
    void should_add_reservation() {
        LocalDate day = LocalDate.of(2024, 5, 15);
        ReservationTime time1 = new ReservationTime(LocalTime.of(10, 0));
        ReservationTime time2 = new ReservationTime(LocalTime.of(11, 0));
        Theme theme = new Theme("무빈테마", "무빈테마설명", "무빈테마썸네일");
        Member member = createMember("무빈", "movin@email.com", "password");

        entityManager.persist(time1);
        entityManager.persist(time2);
        entityManager.persist(theme);
        entityManager.persist(member);

        reservationRepository.save(createAcceptReservation(day, time2, theme, member));

        assertThat(reservationRepository.count()).isEqualTo(1);
    }

    @DisplayName("예약을 삭제한다")
    @Test
    void should_delete_reservation() {
        LocalDate day = LocalDate.of(2024, 5, 15);
        ReservationTime time1 = new ReservationTime(LocalTime.of(10, 0));
        ReservationTime time2 = new ReservationTime(LocalTime.of(11, 0));
        Theme theme = new Theme("무빈테마", "무빈테마설명", "무빈테마썸네일");
        Member member = createMember("무빈", "movin@email.com", "password");

        entityManager.persist(time1);
        entityManager.persist(time2);
        entityManager.persist(theme);
        entityManager.persist(member);

        Reservation reservation1 = createAcceptReservation(day, time1, theme, member);
        Reservation reservation2 = createAcceptReservation(day, time2, theme, member);

        entityManager.persist(reservation1);
        entityManager.persist(reservation2);

        reservationRepository.deleteById(1L);

        assertThat(reservationRepository.count()).isEqualTo(1);
    }

    @DisplayName("아이디에 해당하는 예약 개수를 반환한다.")
    @Test
    void should_return_true_when_id_exist() {
        LocalDate day = LocalDate.of(2024, 5, 15);
        ReservationTime time1 = new ReservationTime(LocalTime.of(10, 0));
        ReservationTime time2 = new ReservationTime(LocalTime.of(11, 0));
        Theme theme = new Theme("무빈테마", "무빈테마설명", "무빈테마썸네일");
        Member member = createMember("무빈", "movin@email.com", "password");

        entityManager.persist(time1);
        entityManager.persist(time2);
        entityManager.persist(theme);
        entityManager.persist(member);

        Reservation reservation1 = createAcceptReservation(day, time1, theme, member);
        Reservation reservation2 = createAcceptReservation(day, time2, theme, member);

        entityManager.persist(reservation1);
        entityManager.persist(reservation2);

        long count = reservationRepository.countById(1L);
        assertThat(count).isEqualTo(1);
    }

    @DisplayName("예약시간에 해당하는 예약 개수를 반환한다.")
    @Test
    void should_return_reservation_count_when_give_time() {
        LocalDate day = LocalDate.of(2024, 5, 15);
        ReservationTime time1 = new ReservationTime(LocalTime.of(10, 0));
        ReservationTime time2 = new ReservationTime(LocalTime.of(11, 0));
        Theme theme = new Theme("무빈테마", "무빈테마설명", "무빈테마썸네일");
        Member member = createMember("무빈", "movin@email.com", "password");

        entityManager.persist(time1);
        entityManager.persist(time2);
        entityManager.persist(theme);
        entityManager.persist(member);

        Reservation reservation1 = createAcceptReservation(day, time1, theme, member);
        Reservation reservation2 = createAcceptReservation(day, time2, theme, member);

        entityManager.persist(reservation1);
        entityManager.persist(reservation2);

        long count = reservationRepository.countByTime(time1);
        assertThat(count).isEqualTo(1);
    }

    @DisplayName("날짜, 시간, 테마에 해당하는 예약 개수를 반환한다.")
    @Test
    void should_return_reservation_count_when_give_date_and_time_and_theme() {
        LocalDate day = LocalDate.of(2024, 5, 15);
        ReservationTime time1 = new ReservationTime(LocalTime.of(10, 0));
        ReservationTime time2 = new ReservationTime(LocalTime.of(11, 0));
        Theme theme = new Theme("무빈테마", "무빈테마설명", "무빈테마썸네일");
        Member member = createMember("무빈", "movin@email.com", "password");

        entityManager.persist(time1);
        entityManager.persist(time2);
        entityManager.persist(theme);
        entityManager.persist(member);

        Reservation reservation1 = createAcceptReservation(day, time1, theme, member);
        Reservation reservation2 = createAcceptReservation(day, time2, theme, member);

        entityManager.persist(reservation1);
        entityManager.persist(reservation2);

        long count = reservationRepository.countByDateAndTimeAndTheme(day, time1, theme);
        assertThat(count).isEqualTo(1);
    }

    @DisplayName("사용자 아이디에 해당하는 예약을 반환한다.")
    @Test
    void should_return_member_reservations() {
        LocalDate day = LocalDate.of(2024, 5, 15);
        ReservationTime time1 = new ReservationTime(LocalTime.of(10, 0));
        ReservationTime time2 = new ReservationTime(LocalTime.of(11, 0));
        Theme theme = new Theme("무빈테마", "무빈테마설명", "무빈테마썸네일");
        Member member = createMember("무빈", "movin@email.com", "password");

        entityManager.persist(time1);
        entityManager.persist(time2);
        entityManager.persist(theme);
        entityManager.persist(member);

        Reservation reservation1 = createAcceptReservation(day, time1, theme, member);
        Reservation reservation2 = createAcceptReservation(day, time2, theme, member);

        entityManager.persist(reservation1);
        entityManager.persist(reservation2);

        List<Reservation> reservations = reservationRepository.findAllByMember(member);
        assertThat(reservations).hasSize(2);
    }

    @DisplayName("날짜, 시간, 테마에 해당하는 예약 개수를 반환한다.")
    @Sql(scripts = {"/init-data.sql", "/member-reservation-test-data.sql"})
    @Test
    void should_return_member_reservation_when_give_member() {
        List<MemberReservation> memberReservation = reservationRepository.findMemberReservation(2L);

        SoftAssertions.assertSoftly(assertions -> {
            assertions.assertThat(memberReservation).hasSize(3);
            assertions.assertThat(memberReservation).extracting("order")
                    .containsExactlyInAnyOrder(0L, 2L, 4L);
        });
    }
}
