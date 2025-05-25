package roomescape.application;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import roomescape.domain.theme.Theme;
import roomescape.domain.theme.ThemeRepository;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class ThemeServiceTest {

    @Autowired
    private ThemeService service;

    @Autowired
    private ThemeRepository themeRepository;

    @Test
    @DisplayName("새로운 테마를 등록할 수 있다.")
    void saveTheme() {
        // given
        var name = "새로운 테마";
        var description = "새로운 테마 설명";
        var thumbnail = "https://new-thumbnail.jpg";

        // when
        Theme created = service.saveTheme(name, description, thumbnail);

        // then
        var themes = themeRepository.findAll();
        assertThat(themes).contains(created);
    }

    @Test
    @DisplayName("모든 테마를 조회할 수 있다.")
    void findAllThemes() {
        // when
        var themes = service.findAllThemes();

        // then
        assertThat(themes).hasSize(3);
    }

    @Test
    @DisplayName("테마를 삭제할 수 있다.")
    void removeById() {
        // given
        var themeId = 3L;

        // when
        service.removeById(themeId);

        // then
        var themes = service.findAllThemes();
        assertThat(themes).hasSize(2);
    }
}
