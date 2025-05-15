package roomescape.unit.fake;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import roomescape.domain.Theme;
import roomescape.domain.repository.ThemeRepository;

class FakeThemeRepositoryTest {

    private final ThemeRepository themeRepository = new FakeThemeRepository();

    @Test
    void 테마를_생성한다() {
        // given
        Theme theme = Theme.createWithoutId("theme1", "desc1", "thumb1");
        // when
        themeRepository.save(theme);
        // then
        List<Theme> allTheme = themeRepository.findAll();
        assertThat(allTheme.getFirst().getName()).isEqualTo("theme1");
    }

    @Test
    void 전체_테마를_조회한다() {
        // given
        Theme theme1 = Theme.createWithoutId("theme1", "desc1", "thumb1");
        themeRepository.save(theme1);
        Theme theme2 = Theme.createWithoutId("theme2", "desc2", "thumb2");
        themeRepository.save(theme2);
        // when
        List<Theme> allTheme = themeRepository.findAll();
        // then
        SoftAssertions soft = new SoftAssertions();
        soft.assertThat(allTheme).hasSize(2);
        soft.assertThat(allTheme.getFirst().getName()).isEqualTo("theme1");
        soft.assertThat(allTheme.get(1).getName()).isEqualTo("theme2");
        soft.assertAll();
    }

    @Test
    void 테마를_삭제한다() {
        // given
        Theme theme1 = Theme.createWithoutId("theme1", "desc1", "thumb1");
        Theme savedTheme = themeRepository.save(theme1);
        // when
        themeRepository.deleteById(savedTheme.getId());
        // then
        List<Theme> allTheme = themeRepository.findAll();
        assertThat(allTheme).hasSize(0);
    }

    @Test
    void id로_테마를_조회한다() {
        // given
        Theme theme1 = Theme.createWithoutId("theme1", "desc1", "thumb1");
        Theme savedTheme = themeRepository.save(theme1);
        // when
        Optional<Theme> optionalTheme = themeRepository.findById(savedTheme.getId());
        // then
        SoftAssertions soft = new SoftAssertions();
        soft.assertThat(optionalTheme).isPresent();
        soft.assertThat(optionalTheme.get().getName()).isEqualTo("theme1");
        soft.assertAll();
    }

    @Test
    void 이름으로_테마를_조회한다() {
        // given
        Theme theme1 = Theme.createWithoutId("theme1", "desc1", "thumb1");
        Theme savedTheme = themeRepository.save(theme1);
        // when
        Optional<Theme> optionalTheme = themeRepository.findByName(savedTheme.getName());
        // then
        SoftAssertions soft = new SoftAssertions();
        soft.assertThat(optionalTheme).isPresent();
        soft.assertThat(optionalTheme.get().getName()).isEqualTo("theme1");
        soft.assertAll();
    }
}