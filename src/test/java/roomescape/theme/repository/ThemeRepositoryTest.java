package roomescape.theme.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import roomescape.theme.domain.Theme;

@DataJpaTest
class ThemeRepositoryTest {

    @Autowired
    private ThemeRepository themeRepository;

    @Test
    void 테마를_저장하고_ID로_조회한다() {
        // given
        Theme theme = Theme.create("공포", "공포 테마입니다.", "horror.jpg");

        // when
        Theme savedTheme = themeRepository.save(theme);
        Optional<Theme> foundTheme = themeRepository.findById(savedTheme.getId());

        // then
        assertThat(foundTheme).isPresent();
        Theme actual = foundTheme.get();
        
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(actual.getName()).isEqualTo("공포");
        softly.assertThat(actual.getDescription()).isEqualTo("공포 테마입니다.");
        softly.assertThat(actual.getThumbnail()).isEqualTo("horror.jpg");
        softly.assertAll();
    }

    @Test
    void 모든_테마를_조회한다() {
        // given
        Theme theme1 = Theme.create("공포", "공포 테마입니다.", "horror.jpg");
        Theme theme2 = Theme.create("추리", "추리 테마입니다.", "detective.jpg");
        Theme theme3 = Theme.create("액션", "액션 테마입니다.", "action.jpg");
        themeRepository.saveAll(List.of(theme1, theme2, theme3));

        // when
        List<Theme> themes = themeRepository.findAll();

        // then
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(themes).hasSize(3);
        softly.assertThat(themes.stream()
                .map(Theme::getName))
                .containsExactlyInAnyOrder("공포", "추리", "액션");
        softly.assertAll();
    }

    @Test
    void 테마를_삭제한다() {
        // given
        Theme theme = Theme.create("공포", "공포 테마입니다.", "horror.jpg");
        Theme savedTheme = themeRepository.save(theme);

        // when
        themeRepository.deleteById(savedTheme.getId());

        // then
        assertThat(themeRepository.findById(savedTheme.getId())).isEmpty();
    }

    @Test
    void 테마를_수정한다() {
        // given
        Theme theme = Theme.create("공포", "공포 테마입니다.", "horror.jpg");
        Theme savedTheme = themeRepository.save(theme);

        // when
        Theme updatedTheme = Theme.builder()
                .id(savedTheme.getId())
                .name("수정된 공포")
                .description("수정된 공포 테마입니다.")
                .thumbnail("updated-horror.jpg")
                .build();
        Theme result = themeRepository.save(updatedTheme);

        // then
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(result.getId()).isEqualTo(savedTheme.getId());
        softly.assertThat(result.getName()).isEqualTo("수정된 공포");
        softly.assertThat(result.getDescription()).isEqualTo("수정된 공포 테마입니다.");
        softly.assertThat(result.getThumbnail()).isEqualTo("updated-horror.jpg");
        softly.assertAll();
    }
} 