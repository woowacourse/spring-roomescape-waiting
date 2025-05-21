package roomescape.domain.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static roomescape.domain.Role.USER;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import roomescape.domain.entity.Member;
import roomescape.domain.entity.Reservation;
import roomescape.domain.ReservationStatus;
import roomescape.domain.entity.ReservationTime;
import roomescape.domain.entity.Theme;

@DataJpaTest
class ThemeRepositoryTest {

    @Autowired
    private ThemeRepository themeRepository;

    @PersistenceContext
    private EntityManager entityManager;

    private Theme theme1;
    private Theme theme2;
    private Theme theme3;
    private ReservationTime time;
    private Member member;
    private LocalDate startDate;
    private LocalDate endDate;

    @BeforeEach
    void setUp() {
        entityManager.createQuery("DELETE FROM Reservation").executeUpdate();
        entityManager.createQuery("DELETE FROM Member").executeUpdate();
        entityManager.createQuery("DELETE FROM Theme").executeUpdate();
        entityManager.createQuery("DELETE FROM ReservationTime").executeUpdate();
        entityManager.flush();
        entityManager.clear();

        theme1 = Theme.withoutId("테마1", "테마1 설명", "썸네일1");
        theme2 = Theme.withoutId("테마2", "테마2 설명", "썸네일2");
        theme3 = Theme.withoutId("테마3", "테마3 설명", "썸네일3");
        entityManager.persist(theme1);
        entityManager.persist(theme2);
        entityManager.persist(theme3);

        time = ReservationTime.withoutId(LocalTime.of(10, 0));
        entityManager.persist(time);

        member = Member.withoutId("member@email.com", "password", "member", USER);
        entityManager.persist(member);

        startDate = LocalDate.now().minusDays(7);
        endDate = LocalDate.now().minusDays(1);
    }

    @DisplayName("특정 기간 동안의 테마별 예약 수를 기준으로 랭킹을 조회한다")
    @Test
    void findThemeRanking() {
        // given
        LocalDate date1 = startDate.plusDays(1);
        LocalDate date2 = startDate.plusDays(2);
        LocalDate date3 = startDate.plusDays(3);

        createReservation(theme1, date1);
        createReservation(theme1, date2);
        createReservation(theme1, date3);

        createReservation(theme2, date1);
        createReservation(theme2, date2);

        createReservation(theme3, date1);

        entityManager.flush();
        entityManager.clear();

        // when
        List<Theme> themeRanking = themeRepository.findThemeRanking(startDate, endDate, 3);

        // then
        assertThat(themeRanking).hasSize(3);
        assertThat(themeRanking).extracting("name")
                .containsExactly("테마1", "테마2", "테마3");
    }

    @DisplayName("예약이 없는 테마도 랭킹에 포함된다")
    @Test
    void findThemeRankingWithNoReservations() {
        // given
        LocalDate date = startDate.plusDays(1);
        createReservation(theme1, date);

        entityManager.flush();
        entityManager.clear();

        // when
        List<Theme> themeRanking = themeRepository.findThemeRanking(startDate, endDate, 3);

        // then
        assertThat(themeRanking).hasSize(3);
        assertThat(themeRanking).extracting("name")
                .containsExactlyInAnyOrder("테마1", "테마2", "테마3");
    }

    private void createReservation(Theme theme, LocalDate date) {
        Reservation reservation = Reservation.withoutId(member, theme, date, time, ReservationStatus.RESERVED);
        entityManager.persist(reservation);
    }
}
