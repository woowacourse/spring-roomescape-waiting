package roomescape.theme.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.global.error.exception.ConflictException;
import roomescape.global.error.exception.NotFoundException;
import roomescape.theme.dto.request.ThemeRequest.ThemeCreateRequest;
import roomescape.theme.repository.ThemeRepository;
import roomescape.theme.service.ThemeService;

@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class ThemeIntegrationTest {

    @Autowired
    private ThemeService themeService;

    @Autowired
    private ThemeRepository themeRepository;

    @Test
    @DisplayName("테마를 생성한다.")
    void createTheme() {
        // given
        var request = new ThemeCreateRequest(
                "미소",
                "미소 테마",
                "https://miso.com"
        );

        // when
        var response = themeService.createTheme(request);

        // then
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("미소");
        assertThat(response.description()).isEqualTo("미소 테마");
        assertThat(response.thumbnail()).isEqualTo("https://miso.com");
    }

    @Test
    @DisplayName("중복되는 테마 이름이 있을 경우 생성할 수 없다.")
    void createThemeWithDuplicateName() {
        // given
        var request1 = new ThemeCreateRequest(
                "미소",
                "미소 테마",
                "https://miso.com"
        );
        themeService.createTheme(request1);

        var request2 = new ThemeCreateRequest(
                "미소",
                "미소 테마2",
                "https://miso2.com"
        );

        // when & then
        assertThatThrownBy(() -> themeService.createTheme(request2))
                .isInstanceOf(ConflictException.class)
                .hasMessage("이미 존재하는 테마 이름입니다.");
    }

    @Test
    @DisplayName("모든 테마를 조회한다.")
    void getAllThemes() {
        // given
        var request1 = new ThemeCreateRequest(
                "미소",
                "미소 테마",
                "https://miso.com"
        );
        var request2 = new ThemeCreateRequest(
                "우테코",
                "우테코 테마",
                "https://wooteco.com"
        );
        themeService.createTheme(request1);
        themeService.createTheme(request2);

        // when
        var responses = themeService.getAllThemes();

        // then
        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).name()).isEqualTo("미소");
        assertThat(responses.get(1).name()).isEqualTo("우테코");
    }

    @Test
    @DisplayName("인기 있는 테마를 조회한다.")
    void getPopularThemes() {
        // given
        var request1 = new ThemeCreateRequest(
                "미소",
                "미소 테마",
                "https://miso.com"
        );
        var request2 = new ThemeCreateRequest(
                "우테코",
                "우테코 테마",
                "https://wooteco.com"
        );
        themeService.createTheme(request1);
        themeService.createTheme(request2);

        // when
        var responses = themeService.getPopularThemes(2);

        // then
        assertThat(responses).hasSize(2);
    }

    @Test
    @DisplayName("테마를 삭제한다.")
    void deleteTheme() {
        // given
        var request = new ThemeCreateRequest(
                "미소",
                "미소 테마",
                "https://miso.com"
        );
        var theme = themeService.createTheme(request);

        // when
        themeService.deleteTheme(theme.id());

        // then
        assertThat(themeService.getAllThemes()).isEmpty();
    }

    @Test
    @DisplayName("존재하지 않는 테마를 삭제하면 예외가 발생한다.")
    void deleteNonExistentTheme() {
        // when & then
        assertThatThrownBy(() -> themeService.deleteTheme(1L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("존재하지 않는 테마입니다.");
    }
}
