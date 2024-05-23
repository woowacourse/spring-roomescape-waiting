package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.reservation.Theme;
import roomescape.repository.ThemeRepository;
import roomescape.service.reservation.dto.ThemeRequest;
import roomescape.service.reservation.dto.ThemeResponse;
import roomescape.service.reservation.ThemeService;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
class ThemeServiceTest {

    @Autowired
    private ThemeService themeService;
    @Autowired
    private ThemeRepository themeRepository;

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
        themeRepository.save(new Theme("happy", "hi", "abcd.html"));

        List<ThemeResponse> themes = themeService.findAllThemes();

        assertThat(themes).hasSize(1);
    }

    @DisplayName("테마를 삭제한다.")
    @Test
    void deleteTheme() {
        Theme theme = themeRepository.save(new Theme("happy", "hi", "abcd.html"));

        themeService.deleteTheme(theme.getId());

        List<Theme> themes = themeRepository.findAll();
        assertThat(themes).isEmpty();
    }
}
