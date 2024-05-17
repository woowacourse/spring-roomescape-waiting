package roomescape.repository;

import java.time.LocalDate;
import java.util.List;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;
import roomescape.domain.theme.Theme;
import roomescape.domain.theme.ThemeRepository;
import roomescape.support.fixture.ThemeFixture;

@DataJpaTest
class ThemeRepositoryTest {

    @Autowired
    private ThemeRepository themeRepository;

    @Test
    @DisplayName("인기 있는 테마들을 조회한다.")
    @Sql("/popular-themes.sql")
    void findPopularThemes() {
        LocalDate startDate = LocalDate.of(2024, 4, 7);
        LocalDate endDate = LocalDate.of(2024, 4, 11);
        int limit = 6;

        List<Theme> popularThemes = themeRepository.findPopularThemes(startDate, endDate, limit);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(popularThemes).hasSize(3);
            softly.assertThat(popularThemes.get(0).getId()).isEqualTo(4);
            softly.assertThat(popularThemes.get(1).getId()).isEqualTo(3);
            softly.assertThat(popularThemes.get(2).getId()).isEqualTo(2);
        });
    }

    @Test
    @DisplayName("name에 해당하는 테마가 존재하는지 확인한다.")
    void existsByName() {
        Theme theme = ThemeFixture.name("테마1");
        themeRepository.save(theme);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(themeRepository.existsByName("테마1")).isTrue();
            softly.assertThat(themeRepository.existsByName("테마2")).isFalse();
        });
    }
}
