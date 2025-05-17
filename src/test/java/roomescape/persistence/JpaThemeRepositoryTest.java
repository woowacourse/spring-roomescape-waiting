package roomescape.persistence;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import roomescape.TestFixture;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@ActiveProfiles("test")
@DataJpaTest
class JpaThemeRepositoryTest {

    @Autowired
    private JpaThemeRepository repository;

    @PersistenceContext
    private EntityManager em;


    @Test
    @DisplayName("인기 테마를 개수에 맞게 조회할 수 있다.")
    void findRankByDateByLimit() {
        // given
        Theme theme1 = TestFixture.createThemeByName("theme1");
        Theme theme2 = TestFixture.createThemeByName("theme2");
        Theme theme3 = TestFixture.createThemeByName("theme3");
        em.persist(theme1);
        em.persist(theme2);
        em.persist(theme3);

        Member member = TestFixture.createDefaultMember();
        em.persist(member);
        ReservationTime time = TestFixture.createDefaultReservationTime();
        em.persist(time);

        Reservation reservation1 = TestFixture.createDefaultReservation(member, LocalDate.of(2025, 1, 1), time, theme1);
        Reservation reservation2 = TestFixture.createDefaultReservation(member, LocalDate.of(2025, 1, 2), time, theme1);
        Reservation reservation3 = TestFixture.createDefaultReservation(member, LocalDate.of(2025, 1, 1), time, theme2);
        em.persist(reservation1);
        em.persist(reservation2);
        em.persist(reservation3);

        // when
        int limit = 2;
        List<Theme> result = repository.findRankByDate(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 7), limit);

        // given
        assertAll(
                () -> assertThat(result).hasSize(2),
                () -> assertThat(result.getFirst()).isEqualTo(theme1)
        );
    }

    @Test
    @DisplayName("인기 테마를 날짜에 맞게 조회할 수 있다.")
    void findRankByDateByDate() {
        // given
        Theme theme1 = TestFixture.createThemeByName("theme1");
        Theme theme2 = TestFixture.createThemeByName("theme2");
        Theme theme3 = TestFixture.createThemeByName("theme3");
        em.persist(theme1);
        em.persist(theme2);
        em.persist(theme3);

        Member member = TestFixture.createDefaultMember();
        em.persist(member);
        ReservationTime time = TestFixture.createDefaultReservationTime();
        em.persist(time);

        Reservation reservation1 = TestFixture.createDefaultReservation(member, LocalDate.of(2025, 1, 1), time, theme1);
        Reservation reservation2 = TestFixture.createDefaultReservation(member, LocalDate.of(2025, 1, 2), time, theme1);
        Reservation reservation3 = TestFixture.createDefaultReservation(member, LocalDate.of(2025, 1, 3), time, theme1);
        Reservation reservation4 = TestFixture.createDefaultReservation(member, LocalDate.of(2025, 1, 1), time, theme2);
        em.persist(reservation1);
        em.persist(reservation2);
        em.persist(reservation3);
        em.persist(reservation4);

        // when
        int limit = 2;
        List<Theme> result = repository.findRankByDate(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 2), limit);

        // given
        assertAll(
                () -> assertThat(result).hasSize(2),
                () -> assertThat(result.getFirst()).isEqualTo(theme1)
        );
    }
}