package roomescape.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import roomescape.domain.Member;
import roomescape.domain.MemberRole;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationStatus;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;

@DataJpaTest
class ThemeRepositoryTest {

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private TestEntityManager em;

    @DisplayName("시작 날짜와 종료 날짜 사이에 예약된 테마를 랭킹 순으로 반환한다.")
    @Test
    void findRankByDate_returnsRankedThemes() {
        // given
        Theme theme1 = em.persist(new Theme(null, "Haunted Mansion", "Ghostly puzzles", "url1"));
        Theme theme2 = em.persist(new Theme(null, "Bank Heist", "Crack the vault", "url2"));
        Member member = em.persist(new Member(null, "User", MemberRole.USER, "user@email.com", "Password1!"));
        ReservationTime time = em.persist(new ReservationTime(null, LocalTime.of(10, 0)));

        LocalDate date = LocalDate.now();
        em.persist(new Reservation(null, member, date, time, theme1, ReservationStatus.RESERVED));
        em.persist(new Reservation(null, member, date, time, theme1, ReservationStatus.RESERVED));
        em.persist(new Reservation(null, member, date, time, theme2, ReservationStatus.RESERVED));
        em.flush();

        // when
        List<Theme> ranked = themeRepository.findRankByDate(date.minusDays(1), date.plusDays(1), 2);

        // then
        assertThat(ranked).hasSize(2);
        assertThat(ranked.get(0).getName()).isEqualTo("Haunted Mansion");
        assertThat(ranked.get(1).getName()).isEqualTo("Bank Heist");
    }
}
