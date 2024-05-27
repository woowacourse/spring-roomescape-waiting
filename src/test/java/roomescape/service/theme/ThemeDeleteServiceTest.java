package roomescape.service.theme;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.jdbc.Sql;
import roomescape.domain.theme.Theme;
import roomescape.repository.ThemeRepository;
import roomescape.service.theme.module.ThemeDeleteService;

@Sql("/all-test-data.sql")
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class ThemeDeleteServiceTest {

    @Autowired
    ThemeDeleteService themeDeleteService;

    @Autowired
    ThemeRepository themeRepository;

    @Test
    void 테마_삭제() {
        //when
        themeDeleteService.deleteTheme(2L);

        //when
        List<Theme> allThemes = themeRepository.findAll();
        assertThat(allThemes).extracting(Theme::getId)
                .isNotEmpty()
                .doesNotContain(2L);
    }

    @Test
    void 예약_되어있는_테마를_삭제할_경우_예외_발생() {
        //then
        assertThatThrownBy(() -> themeDeleteService.deleteTheme(1L))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
