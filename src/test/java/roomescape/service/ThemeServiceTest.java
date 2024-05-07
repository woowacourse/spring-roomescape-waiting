package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import roomescape.service.dto.request.ThemeRequest;
import roomescape.service.dto.response.ThemeResponse;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@Transactional
class ThemeServiceTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ThemeService themeService;

    @DisplayName("테마를 저장한다.")
    @Test
    void createTheme() {
        ThemeRequest themeRequest = new ThemeRequest("happy", "hi", "abcd.html");
        ThemeResponse theme = themeService.createTheme(themeRequest);

        assertAll(
                () -> assertThat(theme.name()).isEqualTo("happy"),
                () -> assertThat(theme.description()).isEqualTo("hi"),
                () -> assertThat(theme.thumbnail()).isEqualTo("abcd.html")
        );
    }

    @DisplayName("모든 테마를 조회한다.")
    @Test
    void findAllThemes() {
        ThemeRequest themeRequest = new ThemeRequest("happy", "hi", "abcd.html");
        ThemeResponse theme = themeService.createTheme(themeRequest);

        List<ThemeResponse> themes = themeService.findAllThemes();

        assertThat(themes).hasSize(1);
    }

    @DisplayName("테마를 삭제한다.")
    @Test
    void deleteTheme() {
        ThemeRequest themeRequest = new ThemeRequest("happy", "hi", "abcd.html");
        ThemeResponse theme = themeService.createTheme(themeRequest);

        themeService.deleteTheme(theme.id());

        List<ThemeResponse> themes = themeService.findAllThemes();
        assertThat(themes).isEmpty();
    }
}
