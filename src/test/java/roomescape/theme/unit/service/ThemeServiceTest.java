package roomescape.theme.unit.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.global.error.exception.ConflictException;
import roomescape.global.error.exception.NotFoundException;
import roomescape.theme.dto.request.ThemeRequest.ThemeCreateRequest;
import roomescape.theme.repository.ThemeRepository;
import roomescape.theme.service.ThemeService;
import roomescape.theme.unit.repository.FakeThemeRepository;

class ThemeServiceTest {

    private ThemeService themeService;
    private ThemeRepository themeRepository;

    @BeforeEach
    void setUp() {
        themeRepository = new FakeThemeRepository();
        themeService = new ThemeService(themeRepository);
    }

    @Test
    @DisplayName("테마를 생성한다.")
    void createTheme() {
        // given
        var request = new ThemeCreateRequest("테마1", "테마1 설명", "테마1 썸네일");

        // when
        var response = themeService.createTheme(request);

        // then
        assertThat(response.name()).isEqualTo("테마1");
        assertThat(response.description()).isEqualTo("테마1 설명");
        assertThat(response.thumbnail()).isEqualTo("테마1 썸네일");
    }

    @Test
    @DisplayName("모든 테마를 조회한다.")
    void getAllThemes() {
        // given
        var request1 = new ThemeCreateRequest("테마1", "테마1 설명", "테마1 썸네일");
        var request2 = new ThemeCreateRequest("테마2", "테마2 설명", "테마2 썸네일");
        themeService.createTheme(request1);
        themeService.createTheme(request2);

        // when
        var responses = themeService.getAllThemes();

        // then
        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).name()).isEqualTo("테마1");
        assertThat(responses.get(1).name()).isEqualTo("테마2");
    }

    @Test
    @DisplayName("인기 테마를 조회한다.")
    void getPopularThemes() {
        // given
        var request1 = new ThemeCreateRequest("테마1", "테마1 설명", "테마1 썸네일");
        var request2 = new ThemeCreateRequest("테마2", "테마2 설명", "테마2 썸네일");
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
        var request = new ThemeCreateRequest("테마1", "테마1 설명", "테마1 썸네일");
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

    @Test
    @DisplayName("중복되는 테마 이름이 있을 경우 생성할 수 없다.")
    void createThemeWithDuplicateName() {
        // given
        var request1 = new ThemeCreateRequest("테마1", "테마1 설명", "테마1 썸네일");
        themeService.createTheme(request1);

        var request2 = new ThemeCreateRequest("테마1", "테마2 설명", "테마2 썸네일");

        // when & then
        assertThatThrownBy(() -> themeService.createTheme(request2))
                .isInstanceOf(ConflictException.class)
                .hasMessage("이미 존재하는 테마 이름입니다.");
    }
}
