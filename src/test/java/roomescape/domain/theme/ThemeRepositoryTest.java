package roomescape.domain.theme;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.domain.BaseRepositoryTest;
import roomescape.domain.member.Member;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.support.fixture.MemberFixture;
import roomescape.support.fixture.ReservationFixture;
import roomescape.support.fixture.ReservationTimeFixture;
import roomescape.support.fixture.ThemeFixture;

class ThemeRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private ThemeRepository themeRepository;

    @Test
    @DisplayName("기간 내의 인기 있는 테마들을 내림차순으로 조회한다.")
    void findPopularThemes() {
        Member member = save(MemberFixture.create());
        ReservationTime reservationTime = save(ReservationTimeFixture.create());
        Theme theme1 = save(ThemeFixture.create("테마1"));
        Theme theme2 = save(ThemeFixture.create("테마2"));
        Theme theme3 = save(ThemeFixture.create("테마3"));
        save(ReservationFixture.create("2024-04-04", member, reservationTime, theme1));
        save(ReservationFixture.create("2024-04-05", member, reservationTime, theme1));
        save(ReservationFixture.create("2024-04-06", member, reservationTime, theme1));
        save(ReservationFixture.create("2024-04-07", member, reservationTime, theme2));
        save(ReservationFixture.create("2024-04-08", member, reservationTime, theme2));
        save(ReservationFixture.create("2024-04-09", member, reservationTime, theme2));
        save(ReservationFixture.create("2024-04-10", member, reservationTime, theme3));
        save(ReservationFixture.create("2024-04-11", member, reservationTime, theme3));
        save(ReservationFixture.create("2024-04-12", member, reservationTime, theme3));

        LocalDate startDate = LocalDate.of(2024, 4, 5);
        LocalDate endDate = LocalDate.of(2024, 4, 10);
        int limit = 2;
        List<Theme> popularThemes = themeRepository.findPopularThemes(startDate, endDate, limit);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(popularThemes).hasSize(2);
            softly.assertThat(popularThemes.get(0).getId()).isEqualTo(theme2.getId());
            softly.assertThat(popularThemes.get(1).getId()).isEqualTo(theme1.getId());
        });
    }

    @Test
    @DisplayName("name에 해당하는 테마가 존재하면 true를 반환한다.")
    void existsByValidName() {
        save(ThemeFixture.create("테마1"));

        assertThat(themeRepository.existsByName(new ThemeName("테마1"))).isTrue();
    }

    @Test
    @DisplayName("name에 해당하는 테마가 존재하지 않으면 false를 반환한다.")
    void existsByInvalidName() {
        save(ThemeFixture.create("테마1"));

        assertThat(themeRepository.existsByName(new ThemeName("테마2"))).isFalse();
    }
}
