package roomescape.theme.domain.repository;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collection;
import java.util.List;
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
class ThemeRepositoryTest {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private ThemeRepository themeRepository;

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

        day1 = LocalDate.now().minusDays(8);
        day2 = LocalDate.now().minusDays(5);
        day3 = LocalDate.now().minusDays(2);

        allReservations = createAndPersistReservations();
    }

    private List<Reservation> createAndPersistReservations() {
        List<Reservation> reservations = List.of(
                new Reservation(member1, day1, time1, theme1),
                new Reservation(member1, day2, time1, theme1),
                new Reservation(member1, day3, time1, theme1),
                new Reservation(member1, day3, time1, theme2),
                new Reservation(member1, day3, time1, theme2),
                new Reservation(member1, day3, time1, theme2),
                new Reservation(member2, day2, time2, theme2),
                new Reservation(member2, day2, time2, theme2),
                new Reservation(member2, day2, time2, theme2),
                new Reservation(member2, day1, time2, theme3),
                new Reservation(member2, day1, time2, theme3),
                new Reservation(member2, day1, time2, theme3)
        );

        reservations.forEach(entityManager::persist);
        return reservations;
    }

    @DisplayName("테마를 저장한다")
    @Test
    void save() {
        // given
        Theme newTheme = new Theme("themeZ", "spooky", "horror");

        // when
        Collection<Theme> themes = themeRepository.findAll();
        themeRepository.save(newTheme);
        Collection<Theme> updatedThemes = themeRepository.findAll();

        // then
        assertThat(updatedThemes).hasSize(themes.size() + 1);
    }

    @DisplayName("특정 기간 내의 상위권 테마를 조회한다")
    @Test
    void findRankedByPeriod() {
        // given
        LocalDate startDate = LocalDate.now().minusDays(7);
        LocalDate endDate = LocalDate.now().minusDays(1);
        int limit = 10;

        // when
        Iterable<Theme> themes = themeRepository.findRankedByPeriod(startDate, endDate, limit);

        // then
        long expectedThemeCount = allReservations.stream()
                .filter(reservation -> !reservation.getDate().isBefore(startDate))
                .filter(reservation -> !reservation.getDate().isAfter(endDate))
                .map(Reservation::getTheme)
                .distinct()
                .count();

        assertThat(themes).hasSize((int) expectedThemeCount);
    }
}
