package roomescape.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import java.util.List;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationStatus;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.fixture.Fixtures;

@DataJpaTest
class ThemeRepositoryTest {
    private final ThemeRepository themeRepository;
    private final EntityManager entityManager;

    private final Member member;
    private final Theme theme1;
    private final Theme theme2;
    private final ReservationTime reservationTime1;
    private final ReservationTime reservationTime2;

    @Autowired
    public ThemeRepositoryTest(ThemeRepository themeRepository, EntityManager entityManager) {
        this.themeRepository = themeRepository;
        this.entityManager = entityManager;

        this.member = Fixtures.member();
        this.theme1 = Fixtures.theme1();
        this.theme2 = Fixtures.theme2();
        this.reservationTime1 = Fixtures.reservationTime1();
        this.reservationTime2 = Fixtures.reservationTime2();
    }

    private void setEnvironmentToTheme1WinAndTheme2Lose() {
        entityManager.persist(member);
        entityManager.persist(theme1);
        entityManager.persist(theme2);
        entityManager.persist(reservationTime1);
        entityManager.persist(reservationTime2);

        Reservation theme1Reservation = new Reservation(
                null,
                member,
                Fixtures.oneDayPlusDate(),
                reservationTime1,
                theme1,
                ReservationStatus.RESERVED
        );

        Reservation theme1Reservation2 = new Reservation(
                null,
                member,
                Fixtures.oneDayPlusDate(),
                reservationTime2,
                theme1,
                ReservationStatus.RESERVED
        );

        Reservation theme2Reservation = new Reservation(
                null,
                member,
                Fixtures.oneDayPlusDate(),
                reservationTime1,
                theme2,
                ReservationStatus.RESERVED
        );

        entityManager.persist(theme1Reservation);
        entityManager.persist(theme1Reservation2);
        entityManager.persist(theme2Reservation);
        entityManager.flush();
    }

    @Test
    void findRankByDate() {
        // given
        setEnvironmentToTheme1WinAndTheme2Lose();

        // when
        List<Theme> rankByDate = themeRepository.findRankByDate(Fixtures.oneDayMinusDate(), Fixtures.twoDayPlusDate(), 10);

        // then
        assertThat(rankByDate.getFirst()).isEqualTo(theme1);
        assertThat(rankByDate.getLast()).isEqualTo(theme2);
    }

    @DisplayName("findRankByDate() 의 limit 파라미터를 테스트한다.")
    @Test
    void findRankByDateWithLimit() {
        // given
        setEnvironmentToTheme1WinAndTheme2Lose();

        // when
        List<Theme> rankByDate = themeRepository.findRankByDate(Fixtures.oneDayMinusDate(), Fixtures.twoDayPlusDate(), 1);

        // then
        assertThat(rankByDate).hasSize(1);
    }

}
