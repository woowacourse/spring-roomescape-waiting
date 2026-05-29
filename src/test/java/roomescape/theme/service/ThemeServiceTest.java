package roomescape.theme.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.exception.ErrorCode;
import roomescape.exception.business.BusinessException;
import roomescape.theme.dto.ThemeResponse;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class ThemeServiceTest {

    @Autowired
    private ThemeService themeService;

    @Test
    @DisplayName("인기 테마 조회")
    void 인기_테마_조회() {
        List<ThemeResponse> result = themeService.getTopThemes(10);

        assertThat(result).hasSize(3);
        assertThat(result.get(0).name()).isEqualTo("테마A");
        assertThat(result.get(1).name()).isEqualTo("테마B");
        assertThat(result.get(2).name()).isEqualTo("테마C");
    }

    @Test
    @DisplayName("인기 테마 조회 limit 적용")
    void 인기_테마_조회_limit_적용() {
        List<ThemeResponse> result = themeService.getTopThemes(2);

        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("id로 테마 조회 성공")
    void getById_성공() {
        assertThat(themeService.getById(1L).getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("존재하지 않는 id로 테마 조회 시 예외 발생")
    void getById_없으면_예외() {
        assertThatThrownBy(() -> themeService.getById(999L))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode()).isEqualTo(ErrorCode.THEME_NOT_FOUND))
                .hasMessage(ErrorCode.THEME_NOT_FOUND.getMessage());
    }
}
