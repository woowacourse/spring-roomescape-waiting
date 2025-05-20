package roomescape.reservation.domain.repository;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collection;
import java.util.List;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
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

    private Member member1;
    private Member member2;
    private ReservationTime time1;
    private ReservationTime time2;
    private Theme theme1;
    private Theme theme2;
    private Theme theme3;
    private LocalDate day1;
    private LocalDate day2;
    private LocalDate day3;
    private List<Reservation> allReservations;

    @BeforeEach
    void setUp() {
        time1 = new ReservationTime(LocalTime.now());
        time2 = new ReservationTime(LocalTime.now().plusHours(1));
        entityManager.persist(time1);
        entityManager.persist(time2);

        theme1 = new Theme("name1", "description1", "thumbnail1");
        theme2 = new Theme("name2", "description2", "thumbnail2");
        theme3 = new Theme("name3", "description3", "thumbnail3");
        entityManager.persist(theme1);
        entityManager.persist(theme2);
        entityManager.persist(theme3);

        member1 = new Member("name1", "email1", "password1");
        member2 = new Member("name2", "email2", "password2");
        entityManager.persist(member1);
        entityManager.persist(member2);

        day1 = LocalDate.now();
        day2 = day1.plusDays(1);
        day3 = day1.plusDays(2);

        allReservations = createAndPersistReservations();
    }

    private List<Reservation> createAndPersistReservations() {
        List<Reservation> reservations = List.of(
                new Reservation(member1, day1, time1, theme1),
                new Reservation(member1, day2, time1, theme1),
                new Reservation(member1, day3, time1, theme1),
                new Reservation(member1, day1, time1, theme2),
                new Reservation(member1, day2, time1, theme2),
                new Reservation(member1, day3, time1, theme2),
                new Reservation(member2, day1, time2, theme2),
                new Reservation(member2, day2, time2, theme2),
                new Reservation(member2, day3, time2, theme2),
                new Reservation(member2, day1, time2, theme3),
                new Reservation(member2, day2, time2, theme3),
                new Reservation(member2, day3, time2, theme3)
        );

        reservations.forEach(entityManager::persist);
        return reservations;
    }

    @DisplayName("예약을 저장한다")
    @Test
    void save() {
        // given
        LocalDate date = LocalDate.parse("2025-05-05");
        Reservation newReservation = new Reservation(member1, date, time1, theme1);

        // when
        reservationRepository.save(newReservation);
        Collection<Reservation> reservations = reservationRepository.findAll();

        // then
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(reservations).hasSize(allReservations.size() + 1);
            softly.assertThat(reservations).contains(newReservation);
        });
    }

    @Test
    @DisplayName("회원 ID만으로 예약 목록을 조회한다")
    void findAllByMemberId() {
        // when
        Collection<Reservation> reservations = reservationRepository.findAllByMemberIdAndThemeIdAndDateBetween(
                member1.getId(), null, null, null
        );

        // then
        long expectedCount = allReservations.stream()
                .filter(reservation -> reservation.getMember().equals(member1))
                .count();

        assertThat(reservations).hasSize((int) expectedCount);
    }

    @Test
    @DisplayName("회원 ID와 테마 ID로 예약 목록을 조회한다")
    void findAllByMemberIdAndThemeId() {
        // when
        Collection<Reservation> reservations = reservationRepository.findAllByMemberIdAndThemeIdAndDateBetween(
                member1.getId(), theme2.getId(), null, null
        );

        // then
        long expectedCount = allReservations.stream()
                .filter(reservation -> reservation.getMember().equals(member1))
                .filter(reservation -> reservation.getTheme().equals(theme2))
                .count();

        assertThat(reservations).hasSize((int) expectedCount);
    }

    @Test
    @DisplayName("회원 ID, 테마 ID, 날짜 기간으로 예약 목록을 조회한다")
    void findAllByMemberIdAndThemeIdAndDateBetween() {
        // when
        LocalDate from = day2;
        LocalDate to = day3;
        Collection<Reservation> reservations = reservationRepository.findAllByMemberIdAndThemeIdAndDateBetween(
                member2.getId(), theme2.getId(), from, to
        );

        // then
        long expectedCount = allReservations.stream()
                .filter(reservation -> reservation.getMember().equals(member2))
                .filter(reservation -> reservation.getTheme().equals(theme2))
                .filter(reservation -> !reservation.getDate().isBefore(from))
                .filter(reservation -> !reservation.getDate().isAfter(to))
                .count();

        assertThat(reservations).hasSize((int) expectedCount);
    }
}
