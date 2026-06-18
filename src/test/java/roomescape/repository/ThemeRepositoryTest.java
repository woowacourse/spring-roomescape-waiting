package roomescape.repository;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Slot;
import roomescape.domain.Theme;

@DataJpaTest
public class ThemeRepositoryTest {

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Test
    void saveTest() {
        Theme themeWithoutId = new Theme("방탈출", "설명", "url.jpg");
        Theme theme = themeRepository.save(themeWithoutId);

        assertThat(theme.getId()).isNotNull();
    }

    @Test
    void findByIdTest() {
        Theme saved = themeRepository.save(new Theme("방탈출1", "방탈출1 설명", "url.jpg"));

        Optional<Theme> theme = themeRepository.findById(saved.getId());

        assertThat(theme.orElseThrow().getId()).isEqualTo(saved.getId());
    }

    @Test
    void findAllTest() {
        themeRepository.save(new Theme("방탈출1", "방탈출1 설명", "url.jpg"));
        themeRepository.save(new Theme("방탈출2", "방탈출2 설명", "url.jpg"));

        List<Theme> themes = themeRepository.findAll();
        assertThat(themes.size()).isEqualTo(2);
    }

    @Test
    void findRankingTest() {
        ReservationTime time1 = reservationTimeRepository.save(new ReservationTime(LocalTime.of(10, 0)));
        ReservationTime time2 = reservationTimeRepository.save(new ReservationTime(LocalTime.of(11, 0)));
        Theme theme1 = themeRepository.save(new Theme("방탈출1", "방탈출1 설명", "url.jpg"));
        Theme theme2 = themeRepository.save(new Theme("방탈출2", "방탈출2 설명", "url.jpg"));
        Member member1 = memberRepository.save(new Member("fizz"));
        Member member2 = memberRepository.save(new Member("tree"));
        Member member3 = memberRepository.save(new Member("neo"));

        reservationRepository.save(new Reservation(member1, new Slot(LocalDate.of(2026, 5, 2), time1, theme1)));
        reservationRepository.save(new Reservation(member2, new Slot(LocalDate.of(2026, 5, 2), time2, theme1)));
        reservationRepository.save(new Reservation(member3, new Slot(LocalDate.of(2026, 5, 2), time1, theme2)));

        List<Theme> themes = themeRepository.findRanking(LocalDate.of(2026, 5, 2), LocalDate.of(2026, 5, 3), 2);

        assertThat(themes.get(0).getId()).isEqualTo(theme1.getId());
        assertThat(themes.get(1).getId()).isEqualTo(theme2.getId());
    }

    @Test
    void deleteByIdTest() {
        Theme saved = themeRepository.save(new Theme("방탈출", "설명", "url.jpg"));

        themeRepository.deleteById(saved.getId());

        assertThat(themeRepository.count()).isEqualTo(0);
    }

    @Test
    void existsByIdTest() {
        Theme saved = themeRepository.save(new Theme("방탈출", "설명", "url.jpg"));

        assertThat(themeRepository.existsById(saved.getId())).isTrue();
        assertThat(themeRepository.existsById(saved.getId() + 1)).isFalse();
    }
}
