package roomescape.domain.repository;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import roomescape.domain.ReservationStatus;
import roomescape.domain.Role;
import roomescape.domain.entity.GameSchedule;
import roomescape.domain.entity.Member;
import roomescape.domain.entity.Reservation;
import roomescape.domain.entity.ReservationTime;
import roomescape.domain.entity.Theme;

@DataJpaTest
class ReservationRepositoryTest {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private ReservationRepository reservationRepository;

    @DisplayName("회원 ID로 예약 목록을 조회한다.")
    @Test
    void findByMember() {
        // given
        Member member1 = Member.withoutId("member1", "member1@email.com", "password", Role.USER);
        Member member2 = Member.withoutId("member2", "member2@email.com", "password", Role.USER);
        entityManager.persist(member1);
        entityManager.persist(member2);

        Theme theme1 = Theme.withoutId("theme1", "description1", "thumbnail1");
        Theme theme2 = Theme.withoutId("theme2", "description2", "thumbnail2");
        entityManager.persist(theme1);
        entityManager.persist(theme2);

        ReservationTime time1 = ReservationTime.withoutId(LocalTime.of(9, 0));
        ReservationTime time2 = ReservationTime.withoutId(LocalTime.of(10, 0));
        entityManager.persist(time1);
        entityManager.persist(time2);

        LocalDate yesterday = LocalDate.now().minusDays(1);
        LocalDate today = LocalDate.now();
        LocalDate tomorrow = LocalDate.now().plusDays(1);

        GameSchedule gameSchedule1 = GameSchedule.withoutId(yesterday, time1, theme1);
        GameSchedule gameSchedule2 = GameSchedule.withoutId(today, time2, theme1);
        GameSchedule gameSchedule3 = GameSchedule.withoutId(tomorrow, time2, theme2);
        entityManager.persist(gameSchedule1);
        entityManager.persist(gameSchedule2);
        entityManager.persist(gameSchedule3);

        Reservation reservation1 = Reservation.withoutId(member1, gameSchedule1, ReservationStatus.RESERVED);
        Reservation reservation2 = Reservation.withoutId(member2, gameSchedule2, ReservationStatus.RESERVED);
        Reservation reservation3 = Reservation.withoutId(member1, gameSchedule3, ReservationStatus.RESERVED);
        entityManager.persist(reservation1);
        entityManager.persist(reservation2);
        entityManager.persist(reservation3);

        // when
        List<Reservation> reservations = reservationRepository.findByMember(member1);

        // then
        assertThat(reservations).hasSize(2);
        assertThat(reservations).allMatch(reservation -> reservation.getMember().equals(member1));
    }

    @DisplayName("테마 ID, 회원 ID, 날짜 범위로 예약 목록을 조회한다.")
    @Test
    void findByMemberAndThemeAndDateRange() {
        // given
        Member member1 = Member.withoutId("member1", "member1@email.com", "password", Role.USER);
        Member member2 = Member.withoutId("member2", "member2@email.com", "password", Role.USER);
        entityManager.persist(member1);
        entityManager.persist(member2);

        Theme theme1 = Theme.withoutId("theme1", "description1", "thumbnail1");
        Theme theme2 = Theme.withoutId("theme2", "description2", "thumbnail2");
        entityManager.persist(theme1);
        entityManager.persist(theme2);

        ReservationTime time1 = ReservationTime.withoutId(LocalTime.of(9, 0));
        ReservationTime time2 = ReservationTime.withoutId(LocalTime.of(10, 0));
        entityManager.persist(time1);
        entityManager.persist(time2);

        LocalDate yesterday = LocalDate.now().minusDays(1);
        LocalDate today = LocalDate.now();
        LocalDate tomorrow = LocalDate.now().plusDays(1);

        GameSchedule gameSchedule1 = GameSchedule.withoutId(yesterday, time1, theme1);
        GameSchedule gameSchedule2 = GameSchedule.withoutId(today, time2, theme1);
        GameSchedule gameSchedule3 = GameSchedule.withoutId(tomorrow, time2, theme2);
        entityManager.persist(gameSchedule1);
        entityManager.persist(gameSchedule2);
        entityManager.persist(gameSchedule3);

        Reservation reservation1 = Reservation.withoutId(member1, gameSchedule1, ReservationStatus.RESERVED);
        Reservation reservation2 = Reservation.withoutId(member2, gameSchedule2, ReservationStatus.RESERVED);
        Reservation reservation3 = Reservation.withoutId(member1, gameSchedule3, ReservationStatus.RESERVED);
        entityManager.persist(reservation1);
        entityManager.persist(reservation2);
        entityManager.persist(reservation3);

        // when
        List<Reservation> reservations = reservationRepository.findByMemberAndThemeAndDateRange(
                member1.getId(),
                theme1.getId(),
                yesterday,
                tomorrow
        );

        // then
        assertThat(reservations).hasSize(1);
        assertThat(reservations).allMatch(reservation ->
                reservation.getMember().getId().equals(member1.getId()) &&
                reservation.getGameSchedule().getTheme().getId().equals(theme1.getId()) &&
                reservation.getGameSchedule().getDate().isAfter(yesterday.minusDays(1)) &&
                reservation.getGameSchedule().getDate().isBefore(tomorrow.plusDays(1)));
    }

    @DisplayName("테마 ID만으로 예약 목록을 조회한다.")
    @Test
    void findByMemberAndGameScheduleAndDateRangeWithThemeIdOnly() {
        // given
        Member member1 = Member.withoutId("member1", "member1@email.com", "password", Role.USER);
        Member member2 = Member.withoutId("member2", "member2@email.com", "password", Role.USER);
        entityManager.persist(member1);
        entityManager.persist(member2);

        Theme theme1 = Theme.withoutId("theme1", "description1", "thumbnail1");
        Theme theme2 = Theme.withoutId("theme2", "description2", "thumbnail2");
        entityManager.persist(theme1);
        entityManager.persist(theme2);

        ReservationTime time1 = ReservationTime.withoutId(LocalTime.of(9, 0));
        ReservationTime time2 = ReservationTime.withoutId(LocalTime.of(10, 0));
        entityManager.persist(time1);
        entityManager.persist(time2);

        LocalDate yesterday = LocalDate.now().minusDays(1);
        LocalDate today = LocalDate.now();
        LocalDate tomorrow = LocalDate.now().plusDays(1);

        GameSchedule gameSchedule1 = GameSchedule.withoutId(yesterday, time1, theme1);
        GameSchedule gameSchedule2 = GameSchedule.withoutId(today, time2, theme1);
        GameSchedule gameSchedule3 = GameSchedule.withoutId(tomorrow, time2, theme2);
        entityManager.persist(gameSchedule1);
        entityManager.persist(gameSchedule2);
        entityManager.persist(gameSchedule3);

        Reservation reservation1 = Reservation.withoutId(member1, gameSchedule1, ReservationStatus.RESERVED);
        Reservation reservation2 = Reservation.withoutId(member2, gameSchedule2, ReservationStatus.RESERVED);
        Reservation reservation3 = Reservation.withoutId(member1, gameSchedule3, ReservationStatus.RESERVED);
        entityManager.persist(reservation1);
        entityManager.persist(reservation2);
        entityManager.persist(reservation3);

        // when
        List<Reservation> reservations = reservationRepository.findByMemberAndThemeAndDateRange(
                null,
                theme1.getId(),
                null,
                null
        );

        // then
        assertThat(reservations).hasSize(2);
        assertThat(reservations).allMatch(
                reservation -> reservation.getGameSchedule().getTheme().getId().equals(theme1.getId()));
    }

    @DisplayName("회원 ID만으로 예약 목록을 조회한다.")
    @Test
    void findByMemberAndGameScheduleAndDateRangeWithMemberIdOnly() {
        // given
        Member member1 = Member.withoutId("member1", "member1@email.com", "password", Role.USER);
        Member member2 = Member.withoutId("member2", "member2@email.com", "password", Role.USER);
        entityManager.persist(member1);
        entityManager.persist(member2);

        Theme theme1 = Theme.withoutId("theme1", "description1", "thumbnail1");
        Theme theme2 = Theme.withoutId("theme2", "description2", "thumbnail2");
        entityManager.persist(theme1);
        entityManager.persist(theme2);

        ReservationTime time1 = ReservationTime.withoutId(LocalTime.of(9, 0));
        ReservationTime time2 = ReservationTime.withoutId(LocalTime.of(10, 0));
        entityManager.persist(time1);
        entityManager.persist(time2);

        LocalDate yesterday = LocalDate.now().minusDays(1);
        LocalDate today = LocalDate.now();
        LocalDate tomorrow = LocalDate.now().plusDays(1);

        GameSchedule gameSchedule1 = GameSchedule.withoutId(yesterday, time1, theme1);
        GameSchedule gameSchedule2 = GameSchedule.withoutId(today, time2, theme1);
        GameSchedule gameSchedule3 = GameSchedule.withoutId(tomorrow, time2, theme2);
        entityManager.persist(gameSchedule1);
        entityManager.persist(gameSchedule2);
        entityManager.persist(gameSchedule3);

        Reservation reservation1 = Reservation.withoutId(member1, gameSchedule1, ReservationStatus.RESERVED);
        Reservation reservation2 = Reservation.withoutId(member2, gameSchedule2, ReservationStatus.RESERVED);
        Reservation reservation3 = Reservation.withoutId(member1, gameSchedule3, ReservationStatus.RESERVED);
        entityManager.persist(reservation1);
        entityManager.persist(reservation2);
        entityManager.persist(reservation3);

        // when
        List<Reservation> reservations = reservationRepository.findByMemberAndThemeAndDateRange(
                member1.getId(),
                null,
                null,
                null
        );

        // then
        assertThat(reservations).hasSize(2);
        assertThat(reservations).allMatch(reservation -> reservation.getMember().getId().equals(member1.getId()));
    }

    @DisplayName("날짜 범위만으로 예약 목록을 조회한다.")
    @Test
    void findByMemberAndGameScheduleAndDateRangeWithDateRangeOnly() {
        // given
        Member member1 = Member.withoutId("member1", "member1@email.com", "password", Role.USER);
        Member member2 = Member.withoutId("member2", "member2@email.com", "password", Role.USER);
        entityManager.persist(member1);
        entityManager.persist(member2);

        Theme theme1 = Theme.withoutId("theme1", "description1", "thumbnail1");
        Theme theme2 = Theme.withoutId("theme2", "description2", "thumbnail2");
        entityManager.persist(theme1);
        entityManager.persist(theme2);

        ReservationTime time1 = ReservationTime.withoutId(LocalTime.of(9, 0));
        ReservationTime time2 = ReservationTime.withoutId(LocalTime.of(10, 0));
        entityManager.persist(time1);
        entityManager.persist(time2);

        LocalDate yesterday = LocalDate.now().minusDays(1);
        LocalDate today = LocalDate.now();
        LocalDate tomorrow = LocalDate.now().plusDays(1);

        GameSchedule gameSchedule1 = GameSchedule.withoutId(yesterday, time1, theme1);
        GameSchedule gameSchedule2 = GameSchedule.withoutId(today, time2, theme1);
        GameSchedule gameSchedule3 = GameSchedule.withoutId(tomorrow, time2, theme2);
        entityManager.persist(gameSchedule1);
        entityManager.persist(gameSchedule2);
        entityManager.persist(gameSchedule3);

        Reservation reservation1 = Reservation.withoutId(member1, gameSchedule1, ReservationStatus.RESERVED);
        Reservation reservation2 = Reservation.withoutId(member2, gameSchedule2, ReservationStatus.RESERVED);
        Reservation reservation3 = Reservation.withoutId(member1, gameSchedule3, ReservationStatus.RESERVED);
        entityManager.persist(reservation1);
        entityManager.persist(reservation2);
        entityManager.persist(reservation3);

        // when
        List<Reservation> reservations = reservationRepository.findByMemberAndThemeAndDateRange(
                null,
                null,
                yesterday,
                tomorrow
        );

        // then
        assertThat(reservations).hasSize(3);
        assertThat(reservations).allMatch(reservation ->
                (reservation.getGameSchedule().getDate().isEqual(yesterday) || reservation.getGameSchedule().getDate()
                        .isAfter(yesterday)) &&
                (reservation.getGameSchedule().getDate().isEqual(tomorrow) || reservation.getGameSchedule().getDate()
                        .isBefore(tomorrow)));
    }
}
