package roomescape.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;
import roomescape.model.Member;
import roomescape.model.Reservation;
import roomescape.model.ReservationTime;
import roomescape.model.Theme;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static roomescape.model.Role.MEMBER;

@DataJpaTest
@Sql(scripts = "/test_data.sql")
class ReservationRepositoryTest {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private ReservationRepository reservationRepository;

    @DisplayName("모든 예약을 조회한다.")
    @Test
    void should_get_all_reservations() {
        LocalDate day = LocalDate.now().plusDays(1);
        ReservationTime time1 = new ReservationTime(LocalTime.of(10, 0));
        ReservationTime time2 = new ReservationTime(LocalTime.of(11, 0));
        Theme theme = new Theme("무빈테마", "무빈테마설명", "무빈테마썸네일");
        Member member = new Member("무빈", MEMBER, "email@email.com", "password");

        entityManager.persist(time1);
        entityManager.persist(time2);
        entityManager.persist(theme);
        entityManager.persist(member);

        Reservation reservation1 = new Reservation(day, time1, theme, member);
        Reservation reservation2 = new Reservation(day, time2, theme, member);

        entityManager.persist(reservation1);
        entityManager.persist(reservation2);

        List<Reservation> reservations = reservationRepository.findAll();
        assertThat(reservations).hasSize(2);
    }

    @DisplayName("특정 날짜와 테마에 해당하는 시간을 조회한다.")
    @Test
    void should_search_reservation_by_condition() {
        LocalDate day = LocalDate.now().plusDays(1);
        ReservationTime time1 = new ReservationTime(LocalTime.of(10, 0));
        ReservationTime time2 = new ReservationTime(LocalTime.of(11, 0));
        Theme theme = new Theme("무빈테마", "무빈테마설명", "무빈테마썸네일");
        Member member = new Member("무빈", MEMBER, "email@email.com", "password");

        entityManager.persist(time1);
        entityManager.persist(time2);
        entityManager.persist(theme);
        entityManager.persist(member);

        Reservation reservation1 = new Reservation(day, time1, theme, member);
        Reservation reservation2 = new Reservation(day, time2, theme, member);

        entityManager.persist(reservation1);
        entityManager.persist(reservation2);

        List<Reservation> reservations = reservationRepository.findAllByDateAndTheme(day, theme);

        assertThat(reservations).hasSize(2);
    }

    @DisplayName("조회한 예약에 예약 시간이 존재한다.")
    @Test
    void should_get_reservation_times() {
        LocalDate day = LocalDate.now().plusDays(1);
        ReservationTime time1 = new ReservationTime(LocalTime.of(10, 0));
        ReservationTime time2 = new ReservationTime(LocalTime.of(11, 0));
        Theme theme = new Theme("무빈테마", "무빈테마설명", "무빈테마썸네일");
        Member member = new Member("무빈", MEMBER, "email@email.com", "password");

        entityManager.persist(time1);
        entityManager.persist(time2);
        entityManager.persist(theme);
        entityManager.persist(member);

        Reservation reservation1 = new Reservation(day, time1, theme, member);
        Reservation reservation2 = new Reservation(day, time2, theme, member);

        entityManager.persist(reservation1);
        entityManager.persist(reservation2);

        List<Reservation> reservations = reservationRepository.findAll();
        assertThat(reservations.get(0).getTime().getStartAt()).isEqualTo(LocalTime.of(10, 0));
    }

    @DisplayName("예약을 추가한다")
    @Test
    void should_add_reservation() {
        LocalDate day = LocalDate.now().plusDays(1);
        ReservationTime time1 = new ReservationTime(LocalTime.of(10, 0));
        ReservationTime time2 = new ReservationTime(LocalTime.of(11, 0));
        Theme theme = new Theme("무빈테마", "무빈테마설명", "무빈테마썸네일");
        Member member = new Member("무빈", MEMBER, "email@email.com", "password");

        entityManager.persist(time1);
        entityManager.persist(time2);
        entityManager.persist(theme);
        entityManager.persist(member);

        reservationRepository.save(new Reservation(day, time2, theme, member));

        assertThat(reservationRepository.count()).isEqualTo(1);
    }

    @DisplayName("예약을 삭제한다")
    @Test
    void should_delete_reservation() {
        LocalDate day = LocalDate.now().plusDays(1);
        ReservationTime time1 = new ReservationTime(LocalTime.of(10, 0));
        ReservationTime time2 = new ReservationTime(LocalTime.of(11, 0));
        Theme theme = new Theme("무빈테마", "무빈테마설명", "무빈테마썸네일");
        Member member = new Member("무빈", MEMBER, "email@email.com", "password");

        entityManager.persist(time1);
        entityManager.persist(time2);
        entityManager.persist(theme);
        entityManager.persist(member);

        Reservation reservation1 = new Reservation(day, time1, theme, member);
        Reservation reservation2 = new Reservation(day, time2, theme, member);

        entityManager.persist(reservation1);
        entityManager.persist(reservation2);

        reservationRepository.deleteById(1L);

        assertThat(reservationRepository.count()).isEqualTo(1);
    }

    @DisplayName("아이디에 해당하는 예약이 존재하면 참을 반환한다.")
    @Test
    void should_return_true_when_id_exist() {
        LocalDate day = LocalDate.now().plusDays(1);
        ReservationTime time1 = new ReservationTime(LocalTime.of(10, 0));
        ReservationTime time2 = new ReservationTime(LocalTime.of(11, 0));
        Theme theme = new Theme("무빈테마", "무빈테마설명", "무빈테마썸네일");
        Member member = new Member("무빈", MEMBER, "email@email.com", "password");

        entityManager.persist(time1);
        entityManager.persist(time2);
        entityManager.persist(theme);
        entityManager.persist(member);

        Reservation reservation1 = new Reservation(day, time1, theme, member);
        Reservation reservation2 = new Reservation(day, time2, theme, member);

        entityManager.persist(reservation1);
        entityManager.persist(reservation2);

        boolean exists = reservationRepository.existsById(1L);
        assertThat(exists).isTrue();
    }

    @DisplayName("예약시간에 해당하는 예약이 존재하면 참을 반환한다.")
    @Test
    void should_return_true_when_time_exist() {
        LocalDate day = LocalDate.now().plusDays(1);
        ReservationTime time1 = new ReservationTime(LocalTime.of(10, 0));
        ReservationTime time2 = new ReservationTime(LocalTime.of(11, 0));
        Theme theme = new Theme("무빈테마", "무빈테마설명", "무빈테마썸네일");
        Member member = new Member("무빈", MEMBER, "email@email.com", "password");

        entityManager.persist(time1);
        entityManager.persist(time2);
        entityManager.persist(theme);
        entityManager.persist(member);

        Reservation reservation1 = new Reservation(day, time1, theme, member);
        Reservation reservation2 = new Reservation(day, time2, theme, member);

        entityManager.persist(reservation1);
        entityManager.persist(reservation2);

        boolean exists = reservationRepository.existsByTime(time1);
        assertThat(exists).isTrue();
    }

    @DisplayName("날짜, 시간, 테마에 해당하는 예약이 존재하면 참을 반환한다.")
    @Test
    void should_return_reservation_count_when_give_date_and_time_and_theme() {
        LocalDate day = LocalDate.now().plusDays(1);
        ReservationTime time1 = new ReservationTime(LocalTime.of(10, 0));
        ReservationTime time2 = new ReservationTime(LocalTime.of(11, 0));
        Theme theme = new Theme("무빈테마", "무빈테마설명", "무빈테마썸네일");
        Member member = new Member("무빈", MEMBER, "email@email.com", "password");

        entityManager.persist(time1);
        entityManager.persist(time2);
        entityManager.persist(theme);
        entityManager.persist(member);

        Reservation reservation1 = new Reservation(day, time1, theme, member);
        Reservation reservation2 = new Reservation(day, time2, theme, member);

        entityManager.persist(reservation1);
        entityManager.persist(reservation2);

        boolean exists = reservationRepository.existsByDateAndTimeAndTheme(day, time1, theme);
        assertThat(exists).isTrue();
    }

    @DisplayName("사용자 아이디에 해당하는 예약을 반환한다.")
    @Test
    void should_return_member_reservations() {
        LocalDate day = LocalDate.now().plusDays(1);
        ReservationTime time1 = new ReservationTime(LocalTime.of(10, 0));
        ReservationTime time2 = new ReservationTime(LocalTime.of(11, 0));
        Theme theme = new Theme("무빈테마", "무빈테마설명", "무빈테마썸네일");
        Member member = new Member("무빈", MEMBER, "email@email.com", "password");

        entityManager.persist(time1);
        entityManager.persist(time2);
        entityManager.persist(theme);
        entityManager.persist(member);

        Reservation reservation1 = new Reservation(day, time1, theme, member);
        Reservation reservation2 = new Reservation(day, time2, theme, member);

        entityManager.persist(reservation1);
        entityManager.persist(reservation2);

        List<Reservation> reservations = reservationRepository.findAllByMember(member);
        assertThat(reservations).hasSize(2);
    }

    @DisplayName("조건에 맞는 예약을 반환한다.")
    @Test
    void should_return_reservations_when_give_conditions() {
        LocalDate day1 = LocalDate.now().plusDays(1);
        LocalDate day2 = LocalDate.now().plusDays(5);
        ReservationTime time = new ReservationTime(LocalTime.of(10, 0));
        Theme theme1 = new Theme("무빈테마", "무빈테마설명", "무빈테마썸네일");
        Theme theme2 = new Theme("배키테마", "배키테마설명", "배키테마썸네일");
        Member member1 = new Member("무빈", MEMBER, "email1@email.com", "password");
        Member member2 = new Member("배키", MEMBER, "email2@email.com", "password");
        entityManager.persist(time);
        entityManager.persist(theme1);
        entityManager.persist(theme2);
        entityManager.persist(member1);
        entityManager.persist(member2);

        Reservation reservation1 = new Reservation(day1, time, theme1, member1);
        Reservation reservation2 = new Reservation(day1.plusDays(1), time, theme1, member2);
        Reservation reservation3 = new Reservation(day1, time, theme2, member1);
        Reservation reservation4 = new Reservation(day1.plusDays(1), time, theme2, member2);
        Reservation reservation5 = new Reservation(day2, time, theme1, member1);
        Reservation reservation6 = new Reservation(day2.minusDays(1), time, theme1, member2);
        Reservation reservation7 = new Reservation(day2, time, theme2, member1);
        Reservation reservation8 = new Reservation(day2.minusDays(1), time, theme2, member2);
        entityManager.persist(reservation1);
        entityManager.persist(reservation2);
        entityManager.persist(reservation3);
        entityManager.persist(reservation4);
        entityManager.persist(reservation5);
        entityManager.persist(reservation6);
        entityManager.persist(reservation7);
        entityManager.persist(reservation8);

        LocalDate middle = LocalDate.now().plusDays(3);

        List<Reservation> reservations = reservationRepository.findByConditions(theme1, member1, day1, middle);
        assertThat(reservations).hasSize(1);
        reservations = reservationRepository.findByConditions(theme1, member1, day1, null);
        assertThat(reservations).hasSize(2);
        reservations = reservationRepository.findByConditions(theme1, member1, null, day2);
        assertThat(reservations).hasSize(2);
        reservations = reservationRepository.findByConditions(theme1, null, day1, day2);
        assertThat(reservations).hasSize(4);
        reservations = reservationRepository.findByConditions(null, member1, day1, day2);
        assertThat(reservations).hasSize(4);
    }

    @DisplayName("날짜, 시간, 테마, 멤버에 해당하는 예약이 존재하면 참을 반환한다.")
    @Test
    void should_return_reservation_count_when_give_date_and_time_and_theme_and_member() {
        LocalDate day = LocalDate.now().plusDays(1);
        ReservationTime time1 = new ReservationTime(LocalTime.of(10, 0));
        ReservationTime time2 = new ReservationTime(LocalTime.of(11, 0));
        Theme theme = new Theme("무빈테마", "무빈테마설명", "무빈테마썸네일");
        Member member = new Member("무빈", MEMBER, "email@email.com", "password");

        entityManager.persist(time1);
        entityManager.persist(time2);
        entityManager.persist(theme);
        entityManager.persist(member);

        Reservation reservation1 = new Reservation(day, time1, theme, member);
        Reservation reservation2 = new Reservation(day, time2, theme, member);

        entityManager.persist(reservation1);
        entityManager.persist(reservation2);

        boolean exists = reservationRepository.existsReservationByThemeAndDateAndTimeAndMember(theme, day, time1, member);
        assertThat(exists).isTrue();
    }
}
