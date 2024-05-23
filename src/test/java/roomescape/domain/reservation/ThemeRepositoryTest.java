package roomescape.domain.reservation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.util.List;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;
import roomescape.domain.exception.DomainNotFoundException;
import roomescape.domain.reservation.detail.Theme;
import roomescape.domain.reservation.detail.ThemeRepository;

@DataJpaTest
class ThemeRepositoryTest {

    @Autowired
    private ThemeRepository themeRepository;

    @Test
    @DisplayName("이름으로 테마가 존재하는지 확인한다.")
    void existsByName() {
        themeRepository.save(new Theme("테마1", "테마1 설명", "https://example1.com"));

        boolean exists = themeRepository.existsByName("테마1");

        assertThat(exists).isTrue();
    }

    @Test
    @Sql("/popular-themes.sql")
    @DisplayName("인기 테마들을 조회한다.")
    void findPopularThemes() {
        LocalDate startDate = LocalDate.of(2024, 4, 6);
        LocalDate endDate = LocalDate.of(2024, 4, 10);
        int limit = 3;

        List<Theme> popularThemes = themeRepository.findPopularThemes(startDate, endDate, limit);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(popularThemes).hasSize(3);

            softly.assertThat(popularThemes.get(0).getId()).isEqualTo(4);
            softly.assertThat(popularThemes.get(0).getName()).isEqualTo("마법의 숲");

            softly.assertThat(popularThemes.get(1).getId()).isEqualTo(3);
            softly.assertThat(popularThemes.get(1).getName()).isEqualTo("시간여행");

            softly.assertThat(popularThemes.get(2).getId()).isEqualTo(2);
            softly.assertThat(popularThemes.get(2).getName()).isEqualTo("우주 탐험");
        });
    }

    @Test
    @DisplayName("아이디로 테마를 조회한다.")
    void getById() {
        Theme savedTheme = themeRepository.save(new Theme("테마1", "테마1 설명", "https://example1.com"));

        Theme theme = themeRepository.getById(savedTheme.getId());

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(theme.getId()).isNotNull();
            softly.assertThat(theme.getName()).isEqualTo("테마1");
            softly.assertThat(theme.getDescription()).isEqualTo("테마1 설명");
            softly.assertThat(theme.getThumbnail()).isEqualTo("https://example1.com");
        });
    }

    @Test
    @DisplayName("아이디로 테마를 조회하고, 없을 경우 예외를 발생시킨다.")
    void getByIdWhenNotExist() {
        themeRepository.save(new Theme("테마1", "테마1 설명", "https://example1.com"));

        assertThatThrownBy(() -> themeRepository.getById(-1L))
                .isInstanceOf(DomainNotFoundException.class);
    }
}
